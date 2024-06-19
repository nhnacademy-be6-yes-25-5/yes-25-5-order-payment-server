package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class FeignClientException extends ApplicationException {

    public FeignClientException(
        ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
