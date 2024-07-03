package com.yes25.yes255orderpaymentserver.application.dto.request;

import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import lombok.Builder;

@Builder
public record UpdateCouponRequest(Long couponId, String operationType) {

    public static UpdateCouponRequest from(Long couponId, OperationType operationType) {
        return UpdateCouponRequest.builder()
            .couponId(couponId)
            .operationType(operationType.name().toLowerCase())
            .build();
    }
}
