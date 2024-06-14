package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class BusinessException extends ApplicationException {

    public BusinessException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
