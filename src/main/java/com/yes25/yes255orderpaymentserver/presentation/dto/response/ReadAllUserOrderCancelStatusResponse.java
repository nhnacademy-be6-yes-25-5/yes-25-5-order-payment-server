package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record ReadAllUserOrderCancelStatusResponse(String orderId,
                                                   Long userId,
                                                   List<String> bookNames,
                                                   List<Long> bookIds,
                                                   LocalDate canceledAt,
                                                   BigDecimal amount,
                                                   PaymentProvider paymentProvider) {

    public static ReadAllUserOrderCancelStatusResponse of(Refund refund, List<Long> bookIds, List<String> bookNames) {
        return ReadAllUserOrderCancelStatusResponse.builder()
            .orderId(refund.getOrder().getOrderId())
            .userId(refund.getOrder().getCustomerId())
            .bookNames(bookNames)
            .bookIds(bookIds)
            .canceledAt(refund.getRequestedAt())
            .amount(refund.getOrder().getOrderTotalAmount())
            .paymentProvider(PaymentProvider.from(refund.getOrder().getPayment().getPaymentProvider()))
            .build();
    }
}
