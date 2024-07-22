package com.yes25.yes255orderpaymentserver.application.service.strategy.payment.impl;

import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.PaymentStrategy;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import org.springframework.stereotype.Component;

@Component("kakao")
public class KakaoPayment implements PaymentStrategy {

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
