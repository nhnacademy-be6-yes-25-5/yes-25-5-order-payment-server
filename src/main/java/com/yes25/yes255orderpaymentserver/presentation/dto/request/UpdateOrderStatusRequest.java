package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.Delivery;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import java.time.LocalDateTime;

public record UpdateOrderStatusRequest(OrderStatusType orderStatusType) {

    public Delivery toEntity(Order order) {
        return Delivery.builder()
            .order(order)
            .timestamp(LocalDateTime.now())
            .deliveryStatus(order.getOrderStatus().getOrderStatusName())
            .build();
    }
}
