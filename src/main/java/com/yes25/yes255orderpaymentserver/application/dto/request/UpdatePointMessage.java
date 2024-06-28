package com.yes25.yes255orderpaymentserver.application.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpdatePointMessage(BigDecimal usePoints, BigDecimal amount) {

    public static UpdatePointMessage of(PreOrder preOrder, BigDecimal purePrice) {
        return UpdatePointMessage.builder()
            .usePoints(preOrder.getPoints())
            .amount(purePrice)
            .build();
    }
}
