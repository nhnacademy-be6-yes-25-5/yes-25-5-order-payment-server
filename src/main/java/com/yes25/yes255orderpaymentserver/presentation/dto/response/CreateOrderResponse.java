package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import lombok.Builder;

@Builder
public record CreateOrderResponse(String orderId) {

    public static CreateOrderResponse fromRequest(String orderId) {
        return CreateOrderResponse.builder()
            .orderId(orderId)
            .build();
    }
}
