package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import lombok.Builder;

@Builder
public record CreateOrderResponse(String orderId, Integer totalAmount) {

    public static CreateOrderResponse fromRequest(PreOrder preOrder) {
        return CreateOrderResponse.builder()
            .orderId(preOrder.getPreOrderId())
            .totalAmount(preOrder.getOrderTotalAmount().intValue())
            .build();
    }
}
