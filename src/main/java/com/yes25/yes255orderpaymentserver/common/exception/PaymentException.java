package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import lombok.Getter;

@Getter
public class PaymentException extends ApplicationException {
    private final String paymentKey;
    private final String orderId;
    private final Integer paymentAmount;

    public PaymentException(ErrorStatus errorStatus, String paymentKey, String orderId,
        Integer paymentAmount) {
        super(errorStatus);
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.paymentAmount = paymentAmount;
    }
}
