package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import lombok.Getter;

@Getter
public class StockUnavailableException extends ApplicationException{
    private final String orderId;

    public StockUnavailableException(
        ErrorStatus errorStatus, String orderId) {
        super(errorStatus);
        this.orderId = orderId;
    }
}
