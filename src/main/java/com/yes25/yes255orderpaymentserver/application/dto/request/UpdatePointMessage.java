package com.yes25.yes255orderpaymentserver.application.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpdatePointMessage(Long userId, BigDecimal usePoints, BigDecimal amount) {

    public static UpdatePointMessage from(PreOrder preOrder, BigDecimal purePrice) {
        return UpdatePointMessage.builder()
            .userId(preOrder.getUserId())
            .usePoints(preOrder.getPoints())
            .amount(purePrice)
            .build();
    }
}
