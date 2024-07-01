package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import java.time.LocalDateTime;

public class PaymentNotFoundException extends EntityNotFoundException{

    public PaymentNotFoundException(
        String message) {
        super(ErrorStatus.toErrorStatus("결제 정보를 찾을 수 없습니다. ID : " + message, 404, LocalDateTime.now()));
    }
}
