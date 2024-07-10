package com.yes25.yes255orderpaymentserver.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://133.186.241.167:6379")
            .setPassword("*N2vya7H@muDTwdNMR!")
            .setConnectionPoolSize(64)
            .setConnectionMinimumIdleSize(24);

        return Redisson.create(config);
    }
}
