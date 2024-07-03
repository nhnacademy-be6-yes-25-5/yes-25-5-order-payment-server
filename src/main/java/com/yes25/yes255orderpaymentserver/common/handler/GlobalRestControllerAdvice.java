package com.yes25.yes255orderpaymentserver.common.handler;

import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.ApplicationException;
import com.yes25.yes255orderpaymentserver.common.exception.StockUnavailableException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalRestControllerAdvice {

    private final MessageProducer messageProducer;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorStatus> handleApplicationException(ApplicationException e) {
        ErrorStatus errorStatus = e.getErrorStatus();

        return new ResponseEntity<>(errorStatus, errorStatus.toHttpStatus());
    }

    @ExceptionHandler(StockUnavailableException.class)
    public ResponseEntity<ErrorStatus> handleStockUnavailableException(StockUnavailableException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        messageProducer.sendOrderCancelMessage(e.getOrderId());

        return new ResponseEntity<>(errorStatus, errorStatus.toHttpStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorStatus> handleAccessDeniedException(AccessDeniedException e) {
        ErrorStatus errorStatus = e.getErrorStatus();

        return new ResponseEntity<>(errorStatus, errorStatus.toHttpStatus());
    }
}
