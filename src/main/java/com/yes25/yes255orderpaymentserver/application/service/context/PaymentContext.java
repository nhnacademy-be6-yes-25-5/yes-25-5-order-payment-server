package com.yes25.yes255orderpaymentserver.application.service.context;

import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.PaymentStrategy;
import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.PaymentStrategyProvider;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentContext {

    private final PaymentStrategyProvider paymentStrategyProvider;

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        PaymentStrategy paymentStrategy = paymentStrategyProvider.getStrategy(
            request.paymentProvider().name().toLowerCase());

        return paymentStrategy.createPayment(request);
    }

    public CreatePaymentResponse createPaymentByZeroAmount(CreatePaymentRequest request) {
        PaymentStrategy paymentStrategy = paymentStrategyProvider.getStrategy(
            request.paymentProvider().name().toLowerCase());

        return paymentStrategy.createPaymentByZeroAmount(request);
    }

    public void cancelPayment(String paymentKey,
        String cancelReason,
        Integer paymentAmount,
        String orderId,
        String paymentProvider) {
        PaymentStrategy paymentStrategy = paymentStrategyProvider.getStrategy(paymentProvider);

        paymentStrategy.cancelPayment(paymentKey, cancelReason, paymentAmount, orderId);
    }
}
