package com.yes25.yes255orderpaymentserver.application.dto.request;


import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import java.util.List;
import lombok.Builder;

@Builder
public record StockRequest(List<Long> bookIdList, List<Integer> quantityList, OperationType operationType) {

    public static StockRequest of(CreatePaymentRequest paymentRequest,
        OperationType operationType) {
        return StockRequest.builder()
            .bookIdList(paymentRequest.bookIds())
            .quantityList(paymentRequest.quantities())
            .operationType(operationType)
            .build();
    }
}
