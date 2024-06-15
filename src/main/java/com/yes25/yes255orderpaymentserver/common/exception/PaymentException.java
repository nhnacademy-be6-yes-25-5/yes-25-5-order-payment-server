package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class PaymentException extends ApplicationException {

    public PaymentException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
