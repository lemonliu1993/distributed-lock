package com.lemon.distributed.lock.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by lemoon on 2023/7/17 23:04
 */
public class DistributedRedisLock implements Lock {

    private StringRedisTemplate redisTemplate;

    public String lockName;

    private String uuid;

    private long expire = 30;

    public DistributedRedisLock(StringRedisTemplate redisTemplate, String lockName, String uuid) {
        this.redisTemplate = redisTemplate;
        this.lockName = lockName;
//        this.uuid = UUID.randomUUID().toString();
        this.uuid = uuid + ":" + Thread.currentThread().getId();
    }


    public void lock() {
        this.tryLock();
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        try {
            return this.tryLock(-1L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 加锁方法
     *
     * @param time
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (time != -1) {
            this.expire = unit.toSeconds(time);
        }
        String script = "if redis.call('exists',KEYS[1]) == 0 or redis.call('hexists',KEYS[1],ARGV[1]) == 1 " +
                "then" +
                " redis.call('hincrby', KEYS[1], ARGV[1],1) " +
                " redis.call('expire', KEYS[1], ARGV[2]) " +
                " return 1 " +
                "else " +
                " return 0 " +
                "end";

        while (!this.redisTemplate.execute(new DefaultRedisScript<Boolean>(script, Boolean.class), Arrays.asList(lockName), uuid, String.valueOf(expire))) {
            Thread.sleep(50);
        }
        //  加锁成功，返回之前，开启定时器自动续期
        this.renewExpire();
        return true;
    }

    /**
     * 解锁方法
     */
    public void unlock() {
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 0 " +
                "then " +
                " return nil " +
                "elseif redis.call('hincrby',KEYS[1], ARGV[1], -1) == 0 " +
                "then " +
                " return redis.call('del',KEYS[1]) " +
                "else " +
                " return 0 " +
                "end";

        Long flag = this.redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(lockName), uuid);
        if (flag == null) {
            throw new IllegalMonitorStateException("this lock doesn't belong to you!");
        }
    }

    public Condition newCondition() {
        return null;
    }


    private void renewExpire() {
        String script = "if redis.call('hexists', KEYS[1],ARGV[1]) == 1 " +
                "then " +
                " return redis.call('expire', KEYS[1],ARGV[2]) " +
                "else " +
                " return 0 " +
                "end";
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, String.valueOf(expire))) {
                    renewExpire();
                }
            }
        }, this.expire * 1000 / 3);
    }

}
