package com.yes25.yes255orderpaymentserver.application.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import java.util.List;
import lombok.Builder;

@Builder
public record SuccessPaymentResponse(String orderId,
                                     String paymentKey,
                                     Integer paymentAmount,
                                     PaymentProvider paymentProvider,
                                     List<Long> bookIdList,
                                     List<Integer> quantityList) {

    public static SuccessPaymentResponse of(Payment payment, CreatePaymentRequest request) {
        return SuccessPaymentResponse.builder()
            .orderId(request.orderId())
            .paymentKey(payment.getPaymentKey())
            .paymentAmount(Integer.valueOf(request.amount()))
            .bookIdList(request.bookIds())
            .paymentProvider(request.paymentProvider())
            .quantityList(request.quantities())
            .build();
    }
}
