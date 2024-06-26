package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;

public interface PaymentProcessor {

    CreatePaymentResponse createPayment(CreatePaymentRequest request);

    void cancelPayment(String paymentKey, String cancelReason, Integer paymentAmount,
        String orderId);
}