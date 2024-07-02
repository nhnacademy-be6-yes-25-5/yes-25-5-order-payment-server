package com.yes25.yes255orderpaymentserver.application.dto.request;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpdateRefundRequest(BigDecimal refundAmount) {

    public static UpdateRefundRequest from(BigDecimal refundAmount) {
        return UpdateRefundRequest.builder()
            .refundAmount(refundAmount)
            .build();
    }
}
