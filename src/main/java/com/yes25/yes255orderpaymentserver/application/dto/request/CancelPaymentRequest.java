package com.yes25.yes255orderpaymentserver.application.dto.request;

import lombok.Builder;

@Builder
public record CancelPaymentRequest(String cancelReason, String cancelAmount) {

    public static CancelPaymentRequest from(String cancelReason, Integer cancelAmount) {
        return CancelPaymentRequest.builder()
            .cancelReason(cancelReason)
//            .cancelAmount(String.valueOf(cancelAmount))
            .build();
    }
}
