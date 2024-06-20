package com.yes25.yes255orderpaymentserver.common.advice;

import com.yes25.yes255orderpaymentserver.application.service.PaymentService;
import com.yes25.yes255orderpaymentserver.application.service.queue.OrderProducer;
import com.yes25.yes255orderpaymentserver.common.exception.ApplicationException;
import com.yes25.yes255orderpaymentserver.common.exception.PaymentException;
import com.yes25.yes255orderpaymentserver.common.exception.StockUnavailableException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalRestControllerAdvice {

    private final PaymentService paymentService;
    private final OrderProducer orderProducer;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorStatus> handleApplicationException(ApplicationException e) {
        ErrorStatus errorStatus = e.getErrorStatus();

        return new ResponseEntity<>(errorStatus, errorStatus.toHttpStatus());
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorStatus> handlePaymentException(PaymentException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        paymentService.cancelPayment(e.getPaymentKey(), e.getErrorStatus().message());

        return new ResponseEntity<>(errorStatus, errorStatus.toHttpStatus());
    }

    @ExceptionHandler(StockUnavailableException.class)
    public ResponseEntity<ErrorStatus> handleStockUnavailableException(StockUnavailableException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        orderProducer.sendCancelMessage(e.getOrderId());

        return new ResponseEntity<>(errorStatus, errorStatus.toHttpStatus());
    }
}
