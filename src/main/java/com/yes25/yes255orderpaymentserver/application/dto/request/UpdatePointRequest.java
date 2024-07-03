package com.yes25.yes255orderpaymentserver.application.dto.request;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpdatePointRequest(BigDecimal usePoints, BigDecimal amount, String operationType) {

    public static UpdatePointRequest from(UpdatePointMessage updatePointMessage) {
        return UpdatePointRequest.builder()
            .usePoints(updatePointMessage.usePoints())
            .amount(updatePointMessage.amount())
            .operationType(updatePointMessage.operationType().toLowerCase())
            .build();
    }
}
