package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateCouponRequest;
import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "couponAdaptor", url = "${api.books-users}", configuration = FeignClientConfig.class)
public interface CouponAdaptor {

    @PatchMapping("/user-coupons")
    void updateCouponStatus(@RequestBody UpdateCouponRequest updateCouponRequest);
}
