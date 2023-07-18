package com.lemon.distributed.lock.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by lemoon on 2023/7/18 10:42
 */

@Component
public class DistributedLockClient {

    @Autowired
    private StringRedisTemplate redisTemplate;


    public DistributedRedisLock getRedisLock(String lockName) {
        return new DistributedRedisLock(redisTemplate, lockName);
    }
}
