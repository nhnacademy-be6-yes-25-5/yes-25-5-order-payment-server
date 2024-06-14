package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class EntityNotFoundException extends DefaultException {

    public EntityNotFoundException(
        ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
