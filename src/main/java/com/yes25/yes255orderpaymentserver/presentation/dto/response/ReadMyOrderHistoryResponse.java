package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ReadMyOrderHistoryResponse(
    String orderId,
    Integer orderTotalAmount,
    LocalDateTime orderCreatedAt,
    LocalDateTime deliveryStartedAt,
    LocalDate orderDeliveryAt,
    OrderStatusType orderStatus,
    List<Long> bookIds,
    List<Integer> quantities,
    List<String> bookNames
) {

    public static ReadMyOrderHistoryResponse of(Order order, List<Long> bookIds, List<Integer> quantities,
        List<String> bookNames) {
        return ReadMyOrderHistoryResponse.builder()
            .orderId(order.getOrderId())
            .orderTotalAmount(order.getOrderTotalAmount().intValue())
            .orderDeliveryAt(order.getOrderDeliveryAt())
            .deliveryStartedAt(order.getDeliveryStartedAt() != null ? order.getDeliveryStartedAt() : LocalDateTime.MIN)
            .orderStatus(OrderStatusType.valueOf(order.getOrderStatus().getOrderStatusName()))
            .bookIds(bookIds)
            .quantities(quantities)
            .bookNames(bookNames)
            .build();
    }
}
