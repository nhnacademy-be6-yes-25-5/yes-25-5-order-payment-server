package com.yes25.yes255orderpaymentserver.common.config;

import com.yes25.yes255orderpaymentserver.common.decoder.CustomErrorDecoder;
import feign.Client;
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
}
