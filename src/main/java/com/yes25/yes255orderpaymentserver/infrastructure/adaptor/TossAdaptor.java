package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import com.yes25.yes255orderpaymentserver.application.dto.request.CancelPaymentRequest;
import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import org.json.simple.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tossAdaptor", url = "https://api.tosspayments.com/v1", configuration = FeignClientConfig.class)
public interface TossAdaptor {

    @PostMapping("/payments/{paymentsKey}/cancel")
    void cancelPayment(@PathVariable String paymentsKey, @RequestBody CancelPaymentRequest request, @RequestHeader String authorization, @RequestHeader String idempotencyKey);

    @PostMapping("/payments/confirm")
    JSONObject confirmPayment(@RequestHeader(name = "Authorization") String authorizations, @RequestBody String request);
}
