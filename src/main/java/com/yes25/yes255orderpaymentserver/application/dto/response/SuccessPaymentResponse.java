package com.yes25.yes255orderpaymentserver.application.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import lombok.Builder;

@Builder
public record SuccessPaymentResponse(String orderId, String paymentKey, Integer paymentAmount) {

    public static SuccessPaymentResponse fromEntity(Payment payment) {
        return SuccessPaymentResponse.builder()
            .orderId(payment.getOrderId())
            .paymentKey(payment.getPaymentKey())
            .paymentAmount(payment.getPaymentAmount().intValue())
            .build();
    }
}
