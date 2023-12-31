package com.lemon.distributed.lock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lemoon on 2023/7/21 08:51
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        //初始化一个配置对象
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")  //redis服务器地址
                .setDatabase(0) //指定redis数据库编号
//                .setConnectionMinimumIdleSize(10)   //连接池最小空闲连接数
//                .setConnectionPoolSize(50)  //连接池最大线程数
//                .setIdleConnectionTimeout(60000)    //线程超时时间
//                .setConnectTimeout()    //客户端程序获取redis连接的超时时间
//                .setTimeout()       //响应超时时间
        ;
        return Redisson.create(config);
    }
}
