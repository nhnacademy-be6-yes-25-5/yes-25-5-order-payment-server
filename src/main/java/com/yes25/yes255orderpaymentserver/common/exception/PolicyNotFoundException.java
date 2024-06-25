package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class PolicyNotFoundException extends EntityNotFoundException {

    public PolicyNotFoundException(
        ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
