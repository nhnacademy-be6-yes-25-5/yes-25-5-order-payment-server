package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;

public interface PaymentService {

    CreatePaymentResponse createPayment(CreatePaymentRequest request);

    void cancelPayment(String paymentKey, String message);
}
