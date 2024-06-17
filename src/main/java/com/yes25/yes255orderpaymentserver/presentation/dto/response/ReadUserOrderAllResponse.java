package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ReadUserOrderAllResponse(String orderId, LocalDateTime orderCreatedAt, List<Long> productIds,
                                       BigDecimal amount, OrderStatusType orderStatusType) {

    public static ReadUserOrderAllResponse fromEntity(Order order, List<OrderBook> orderBooks) {
        List<Long> productIds = orderBooks.stream()
            .map(OrderBook::getBookId)
            .toList();

        return ReadUserOrderAllResponse.builder()
            .orderId(order.getOrderId())
            .orderCreatedAt(order.getOrderCreatedAt())
            .productIds(productIds)
            .amount(order.getOrderTotalAmount())
            .orderStatusType(OrderStatusType.valueOf(order.getOrderStatus().getOrderStatusName()))
            .build();
    }
}
