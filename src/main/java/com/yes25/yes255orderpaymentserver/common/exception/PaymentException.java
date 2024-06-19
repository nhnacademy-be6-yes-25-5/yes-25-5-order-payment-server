package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import lombok.Getter;

@Getter
public class PaymentException extends ApplicationException {
    private final String paymentKey;

    public PaymentException(ErrorStatus errorStatus, String paymentKey) {
        super(errorStatus);
        this.paymentKey = paymentKey;
    }
}
