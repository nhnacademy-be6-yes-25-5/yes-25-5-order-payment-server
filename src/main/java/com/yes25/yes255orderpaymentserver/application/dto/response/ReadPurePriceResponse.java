package com.yes25.yes255orderpaymentserver.application.dto.response;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ReadPurePriceResponse(BigDecimal purePrice, Long customerId) {

    public static ReadPurePriceResponse from(BigDecimal purePriceWithCancel, Long orderUserId) {
        return ReadPurePriceResponse.builder()
            .purePrice(purePriceWithCancel)
            .customerId(orderUserId)
            .build();
    }
}
