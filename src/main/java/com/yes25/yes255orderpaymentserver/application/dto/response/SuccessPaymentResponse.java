package com.yes25.yes255orderpaymentserver.application.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import java.util.List;
import lombok.Builder;

@Builder
public record SuccessPaymentResponse(String orderId,
                                     String paymentKey,
                                     Integer paymentAmount,
                                     List<Long> bookIdList,
                                     List<Integer> quantityList) {

    public static SuccessPaymentResponse of(Payment payment, CreatePaymentRequest request) {
        return SuccessPaymentResponse.builder()
            .orderId(payment.getPreOrderId())
            .paymentKey(payment.getPaymentKey())
            .paymentAmount(payment.getPaymentAmount().intValue())
            .bookIdList(request.bookIds())
            .quantityList(request.quantities())
            .build();
    }
}
