package com.yes25.yes255orderpaymentserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//@EnableDiscoveryClient
public class Yes255OrderPaymentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(Yes255OrderPaymentServerApplication.class, args);
    }
}
