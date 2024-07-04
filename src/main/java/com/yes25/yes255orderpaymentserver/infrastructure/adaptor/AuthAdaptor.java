package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;


import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.JwtAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "authAdaptor", url = "${api.auth}/auth", configuration = FeignClientConfig.class)
public interface AuthAdaptor {

    @GetMapping("/info")
    JwtAuthResponse getUserInfoByUUID(@RequestParam String uuid);
}
