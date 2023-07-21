package com.lemon.distributed.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lemon.distributed.lock.mapper.StockMapper;
import com.lemon.distributed.lock.pojo.Stock;
import com.lemon.distributed.lock.util.DistributedLockClient;
import com.lemon.distributed.lock.util.DistributedRedisLock;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lemoon on 2023/6/20 10:08
 */
@Service
public class StockService {
//    private Stock stock = new Stock();

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLockClient distributedLockClient;

    @Autowired
    private RedissonClient redissonClient;

    public void deduct() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock(10, TimeUnit.SECONDS);
        try {
            //  1.查询库存信息
            String stock = redisTemplate.opsForValue().get("stock").toString();

            //  2.判断库存是否充足
            if (stock != null && stock.length() != 0) {
                Integer st = Integer.valueOf(stock);
                if (st > 0) {
                    //  3.扣减库存
                    redisTemplate.opsForValue().set("stock", String.valueOf(--st));
                }
            }
//            this.test();
        } finally {
            lock.unlock();
        }
    }


    public void deduct8() {

        DistributedRedisLock redisLock = this.distributedLockClient.getRedisLock("lock");
        redisLock.lock();
        try {
            //  1.查询库存信息
            String stock = redisTemplate.opsForValue().get("stock").toString();

            //  2.判断库存是否充足
            if (stock != null && stock.length() != 0) {
                Integer st = Integer.valueOf(stock);
                if (st > 0) {
                    //  3.扣减库存
                    redisTemplate.opsForValue().set("stock", String.valueOf(--st));
                }
            }
            this.test();
        } finally {
            redisLock.unlock();
        }
    }

    public void test() {
        DistributedRedisLock lock = this.distributedLockClient.getRedisLock("lock");
        lock.lock();
        System.out.println("测试可重入锁");
        lock.unlock();
    }

    public void deduct7() {
        String uuid = UUID.randomUUID().toString();
        //  加锁setnx
        while (!this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 5, TimeUnit.SECONDS)) {
            //  重试:循环
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            //  1.查询库存信息
            String stock = redisTemplate.opsForValue().get("stock").toString();

            //  2.判断库存是否充足
            if (stock != null && stock.length() != 0) {
                Integer st = Integer.valueOf(stock);
                if (st > 0) {
                    //  3.扣减库存
                    redisTemplate.opsForValue().set("stock", String.valueOf(--st));
                }
            }
        } finally {
            //  先判断是否自己的锁，再解锁
            String script = "if redis.call('get' ,KEYS[1]) == ARGV[1] " +
                    "then " +
                    " return redis.call('del', KEYS[1]) " +
                    "else " +
                    " return 0 " +
                    "end";
            this.redisTemplate.execute(new DefaultRedisScript(script, Boolean.class), Arrays.asList("lock"), uuid);
//            if (StringUtils.equals(this.redisTemplate.opsForValue().get("lock"), uuid)) {
//                //  解锁
//                this.redisTemplate.delete("lock");
//            }
        }


    }


    public void deduct6() {
        //  加锁setnx
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", "111");
        //  重试，递归调用
        if (!lock) {
            try {
                Thread.sleep(50);
                this.deduct();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                //  1.查询库存信息
                String stock = redisTemplate.opsForValue().get("stock").toString();

                //  2.判断库存是否充足
                if (stock != null && stock.length() != 0) {
                    Integer st = Integer.valueOf(stock);
                    if (st > 0) {
                        //  3.扣减库存
                        redisTemplate.opsForValue().set("stock", String.valueOf(--st));
                    }
                }
            } finally {
                //  解锁
                this.redisTemplate.delete("lock");
            }
        }


    }


    public void deduct5() {
        this.redisTemplate.execute(new SessionCallback<Object>() {
            public Object execute(RedisOperations redisTemplate) throws DataAccessException {
                //  watch
                redisTemplate.watch("stock");
                //1.查询库存信息
                String stock = redisTemplate.opsForValue().get("stock").toString();
                System.out.println("stock:" + stock);
                //2.判断库存是否充足
                if (stock != null && stock.length() != 0) {
                    Integer st = Integer.valueOf(stock);
                    if (st > 0) {
                        //  multi
                        redisTemplate.multi();
                        //3.扣减库存
                        redisTemplate.opsForValue().set("stock", String.valueOf(--st));
                        //exec执行事务
                        List exec = redisTemplate.exec();
                        if (exec == null || exec.size() == 0) {
                            try {
                                Thread.sleep(40);
                                deduct5();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        return exec;
                    }
                }

                return null;

            }
        });


    }


    public void deduct4() {
        //1.查询库存信息
        String stock = this.redisTemplate.opsForValue().get("stock");
        System.out.println("stock:" + stock);
        //2.判断库存是否充足
        if (stock != null && stock.length() != 0) {
            Integer st = Integer.valueOf(stock);
            if (st > 0) {
                //3.扣减库存
                this.redisTemplate.opsForValue().set("stock", String.valueOf(--st));
            }
        }
    }


    private ReentrantLock lock = new ReentrantLock();


    //    @Transactional
    public void deduct3() {
        //1.查询库存信息并锁定库存信息
        List<Stock> stocks = this.stockMapper.selectList(new QueryWrapper<Stock>().eq("product_code", "1001"));
        //这里取第一个库存
        Stock stock = stocks.get(0);
        //2.判断库存是否充足
        if (stock != null && stock.getCount() > 0) {
            //3.扣减库存
            stock.setCount(stock.getCount() - 1);
            Integer version = stock.getVersion();
            stock.setVersion(version + 1);
            //mybatis 能获得sql的返回值，0为更新条数为0
            if (this.stockMapper.update(stock, new UpdateWrapper<Stock>().eq("id", stock.getId()).eq("version", version)) == 0) {
                //  如果更新失败，则进行重试
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.deduct();
            }
        }
    }

    @Transactional
    public void deduct2() {
        //1.查询库存信息并锁定库存信息
        List<Stock> stocks = this.stockMapper.queryStock("1001");
        //这里取第一个库存
        Stock stock = stocks.get(0);
        //2.判断库存是否充足
        if (stock != null && stock.getCount() > 0) {
            //3.扣减库存
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }
    }


    //    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void deduct1() {
        lock.lock();
        try {
            stockMapper.updateStock("1001", 1);
            System.out.println(stockMapper.selectById(1));

            /**
             //  update insert delete 写操作本身就会加锁
             //  update db_stock set count = count -1 where product_code= '10001' and count >= 1
             //  1.查询库存
             Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
             //  2.判断库存是否充足
             if (stock != null && stock.getCount() > 0) {
             stock.setCount(stock.getCount() - 1);
             //  3.更新到数据库
             stockMapper.updateById(stock);
             }
             */
//        stock.setStock(stock.getStock()-1);
//        System.out.println("库存余量： "+ stock.getStock());

        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        /**
         ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
         System.out.println("定时任务初始时间:" + System.currentTimeMillis());
         scheduledExecutorService.scheduleAtFixedRate(() -> {
         System.out.println("定时任务的执行时间: " + System.currentTimeMillis());
         }, 5, 10, TimeUnit.SECONDS

         );
         */
        System.out.println("定时任务初始时间:" + System.currentTimeMillis());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("定时任务执行时间: " + System.currentTimeMillis());
            }
        }, 5000, 10000);

    }
}
