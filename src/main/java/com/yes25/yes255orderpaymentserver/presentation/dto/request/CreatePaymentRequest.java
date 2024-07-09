package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreatePaymentRequest(String paymentKey,
                                   String orderId,
                                   String amount,
                                   List<Long> bookIds,
                                   List<Integer> quantities) {

    public Payment toEntity() {
        return Payment.builder()
            .preOrderId(orderId)
            .paymentAmount(BigDecimal.ZERO)
            .paymentKey(paymentKey)
            .requestedAt(LocalDateTime.now())
            .approveAt(LocalDateTime.now())
            .paymentMethod("일반 결제 - 0원")
            .build();
    }
}
