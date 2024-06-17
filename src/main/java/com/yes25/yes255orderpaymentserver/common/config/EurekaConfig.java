package com.yes25.yes255orderpaymentserver.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "eureka.client.enabled", havingValue = "true", matchIfMissing = true)
@EnableDiscoveryClient
public class EurekaConfig {
}
