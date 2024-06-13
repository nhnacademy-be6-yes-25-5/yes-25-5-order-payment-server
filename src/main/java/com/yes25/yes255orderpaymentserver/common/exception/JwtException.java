package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class JwtException extends ApplicationException {

    public JwtException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
