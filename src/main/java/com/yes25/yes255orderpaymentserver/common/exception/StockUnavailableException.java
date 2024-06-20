package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;

public class StockUnavailableException extends ApplicationException{

    public StockUnavailableException(
        ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
