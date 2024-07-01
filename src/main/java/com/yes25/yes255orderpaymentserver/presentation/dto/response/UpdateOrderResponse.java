package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import lombok.Builder;

@Builder
public record UpdateOrderResponse(String message) {

    public static UpdateOrderResponse from(String message) {
        return UpdateOrderResponse.builder()
            .message(message)
            .build();
    }
}
