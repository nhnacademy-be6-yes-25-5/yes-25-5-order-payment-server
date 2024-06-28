package com.yes25.yes255orderpaymentserver.common.config;

import com.yes25.yes255orderpaymentserver.common.decoder.CustomErrorDecoder;
import com.yes25.yes255orderpaymentserver.common.interceptor.JwtAuthorizationRequestInterceptor;
import feign.Client;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public Client okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public JwtAuthorizationRequestInterceptor jwtAuthorizationRequestInterceptor() {
        return new JwtAuthorizationRequestInterceptor();
    }

    @Bean
    public RequestInterceptor requestInterceptor(
        JwtAuthorizationRequestInterceptor interceptor) {
        return interceptor;
    }
}
