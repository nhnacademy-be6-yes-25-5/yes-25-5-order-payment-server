package com.yes25.yes255orderpaymentserver.application.dto.request;

import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpdatePointMessage(BigDecimal usePoints, BigDecimal amount, String operationType) {

    public static UpdatePointMessage of(BigDecimal points, BigDecimal purePrice, OperationType operationType) {
        return UpdatePointMessage.builder()
            .usePoints(points)
            .amount(purePrice)
            .operationType(operationType.name())
            .build();
    }
}
