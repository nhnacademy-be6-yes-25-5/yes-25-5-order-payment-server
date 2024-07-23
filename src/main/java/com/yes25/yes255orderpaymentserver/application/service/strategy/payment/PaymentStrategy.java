package com.yes25.yes255orderpaymentserver.application.service.strategy.payment;

import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;

public interface PaymentStrategy {

    CreatePaymentResponse createPayment(CreatePaymentRequest request);

    void cancelPayment(String paymentKey, String cancelReason, Integer paymentAmount,
        String orderId);

    CreatePaymentResponse createPaymentByZeroAmount(CreatePaymentRequest request);
}
