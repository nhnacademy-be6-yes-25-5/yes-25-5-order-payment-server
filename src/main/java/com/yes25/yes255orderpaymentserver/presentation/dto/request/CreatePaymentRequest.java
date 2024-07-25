package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PaymentDetail;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record CreatePaymentRequest(String paymentKey,
                                   String orderId,
                                   String amount,
                                   PaymentProvider paymentProvider,
                                   List<Long> bookIds,
                                   List<Integer> quantities) {

    public Payment toEntity() {
        return Payment.builder()
            .preOrderId(orderId)
            .paymentKey(paymentKey)
            .paymentProvider(paymentProvider.name().toLowerCase())
            .build();
    }

    public PaymentDetail zeroPay(Payment payment) {
        return PaymentDetail.builder()
            .paymentAmount(BigDecimal.ZERO)
            .approveAt(LocalDateTime.now())
            .requestedAt(LocalDateTime.now())
            .paymentMethod("0원 결제")
            .payment(payment)
            .build();
    }
}
