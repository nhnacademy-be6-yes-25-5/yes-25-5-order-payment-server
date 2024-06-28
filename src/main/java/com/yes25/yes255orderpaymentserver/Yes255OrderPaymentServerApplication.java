package com.yes25.yes255orderpaymentserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class Yes255OrderPaymentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(Yes255OrderPaymentServerApplication.class, args);
    }
}
