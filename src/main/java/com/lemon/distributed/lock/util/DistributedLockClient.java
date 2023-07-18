package com.lemon.distributed.lock.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by lemoon on 2023/7/18 10:42
 */

@Component
public class DistributedLockClient {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String uuid;


    public DistributedLockClient() {
        this.uuid = UUID.randomUUID().toString();
        // 这个UUID在spring容器初始化时就已经创建好了，保证了一个服务器的uuid都是一样的
        // 同一个服务中用ThreadId区分
        // 不同服务中首先uuid就不同了，就区分开了
    }


    public DistributedRedisLock getRedisLock(String lockName) {
        return new DistributedRedisLock(redisTemplate, lockName, uuid);
    }
}
