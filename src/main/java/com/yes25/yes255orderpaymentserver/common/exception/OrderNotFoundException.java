package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class OrderNotFoundException extends EntityNotFoundException{

    public OrderNotFoundException(
        ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
