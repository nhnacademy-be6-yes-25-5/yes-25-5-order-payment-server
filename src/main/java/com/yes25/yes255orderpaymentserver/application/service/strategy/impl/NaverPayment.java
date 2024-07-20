package com.yes25.yes255orderpaymentserver.application.service.strategy.impl;

import com.yes25.yes255orderpaymentserver.application.service.strategy.PaymentStrategy;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component("naver")
public class NaverPayment implements PaymentStrategy {

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        return null;
    }

    @Override
    public void cancelPayment(String paymentKey, String cancelReason, Integer paymentAmount,
        String orderId) {

    }

    @Override
    public CreatePaymentResponse createPaymentByZeroAmount(CreatePaymentRequest request) {
        return null;
    }
}
