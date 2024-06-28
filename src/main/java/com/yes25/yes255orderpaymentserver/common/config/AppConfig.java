package com.yes25.yes255orderpaymentserver.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.RequestContextFilter;

@Configuration
public class AppConfig {

    @Bean
    public RequestContextFilter requestContextFilter() {
        return new RequestContextFilter();
    }
}
