package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import java.time.LocalDateTime;

public class AccessDeniedException extends ApplicationException {

    public AccessDeniedException(String message) {
        super(ErrorStatus.toErrorStatus(message, 403, LocalDateTime.now()));
    }
}
