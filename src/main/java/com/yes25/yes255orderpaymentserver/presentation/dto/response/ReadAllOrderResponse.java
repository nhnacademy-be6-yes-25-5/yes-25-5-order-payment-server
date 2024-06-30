package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ReadAllOrderResponse(String orderId,
                                   Long customerId,
                                   List<String> bookNames,
                                   List<Long> bookIds,
                                   List<Integer> quantities,
                                   LocalDateTime orderCreatedAt,
                                   LocalDate orderDeliveryAt,
                                   BigDecimal amount,
                                   OrderStatusType orderStatusType,
                                   String role) {

    public static ReadAllOrderResponse of(Order order, List<Long> bookIds, List<Integer> quantities, List<String> bookNames) {
        return ReadAllOrderResponse.builder()
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .bookNames(bookNames)
            .bookIds(bookIds)
            .quantities(quantities)
            .orderCreatedAt(order.getOrderCreatedAt())
            .orderDeliveryAt(order.getOrderDeliveryAt())
            .amount(order.getOrderTotalAmount())
            .orderStatusType(OrderStatusType.valueOf(order.getOrderStatus().getOrderStatusName()))
            .role(order.getUserRole().toLowerCase())
            .build();
    }
}
