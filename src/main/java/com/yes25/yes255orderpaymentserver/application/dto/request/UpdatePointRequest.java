package com.yes25.yes255orderpaymentserver.application.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpdatePointRequest(BigDecimal usedPoints, BigDecimal amount) {

    public static UpdatePointRequest from(PreOrder preOrder) {
        return UpdatePointRequest.builder()
            .usedPoints(preOrder.getPoints())
            .amount(preOrder.getOrderTotalAmount())
            .build();
    }
}
