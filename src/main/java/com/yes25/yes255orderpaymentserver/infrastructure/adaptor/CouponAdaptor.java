package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "couponAdaptor", url = "${api.coupon}", configuration = FeignClientConfig.class)
public interface CouponAdaptor {

    @PatchMapping("/user-coupons/{couponId}/status")
    void updateCouponStatus(@PathVariable Long couponId);
}
