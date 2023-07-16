package com.lemon.distributed.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lemon.distributed.lock.mapper.StockMapper;
import com.lemon.distributed.lock.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
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

    public void deduct() {
        //1.查询库存信息
        String stock = this.redisTemplate.opsForValue().get("stock");
        System.out.println("stock:" + stock);
        //2.判断库存是否充足
        if(stock != null && stock.length() != 0){
            Integer st = Integer.valueOf(stock);
            if(st>0){
                //3.扣减库存
                this.redisTemplate.opsForValue().set("stock",String.valueOf(--st));
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
}
