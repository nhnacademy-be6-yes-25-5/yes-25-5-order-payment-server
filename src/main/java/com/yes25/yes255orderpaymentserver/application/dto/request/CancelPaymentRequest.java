package com.yes25.yes255orderpaymentserver.application.dto.request;

import lombok.Builder;

@Builder
public record CancelPaymentRequest(String cancelReason, Integer cancelAmount) {

    public static CancelPaymentRequest from(String cancelReason, Integer cancelAmount) {
        return CancelPaymentRequest.builder()
            .cancelReason(cancelReason)
            .cancelAmount(cancelAmount)
            .build();
    }
}
