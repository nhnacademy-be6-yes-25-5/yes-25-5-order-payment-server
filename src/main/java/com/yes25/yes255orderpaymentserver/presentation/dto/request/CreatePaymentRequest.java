package com.yes25.yes255orderpaymentserver.presentation.dto.request;

public record CreatePaymentRequest(String paymentKey, String orderId, String amount) {

}
