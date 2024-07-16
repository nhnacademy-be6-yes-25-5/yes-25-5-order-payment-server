package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.KeyManagerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "keyManagerAdaptor", url = "${nhn.cloud.secure.url}/secrets", configuration = FeignClientConfig.class)
public interface KeyManagerAdaptor {

    @GetMapping("/{keyId}")
    KeyManagerResponse getSecret(@PathVariable String keyId);
}
