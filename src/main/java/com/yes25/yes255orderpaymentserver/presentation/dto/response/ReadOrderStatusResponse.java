package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import lombok.Builder;

@Builder
public record ReadOrderStatusResponse(String orderId, int status) {

    public static ReadOrderStatusResponse fromEntity(Order order) {
        return ReadOrderStatusResponse.builder()
            .orderId(order.getOrderId())
            .status(200)
            .build();
    }
}
