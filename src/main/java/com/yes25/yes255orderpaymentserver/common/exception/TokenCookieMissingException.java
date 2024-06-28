package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class TokenCookieMissingException extends ApplicationException{

    public TokenCookieMissingException(
        ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
