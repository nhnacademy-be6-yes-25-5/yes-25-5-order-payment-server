package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;


public class DefaultException extends ApplicationException {

    public DefaultException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
