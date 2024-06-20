package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ReadUserOrderAllResponse(String orderId,
                                       List<Long> productIds,
                                       List<Integer> quantities,
                                       LocalDate orderCreatedAt,
                                       LocalDate orderDeliveryAt,
                                       BigDecimal amount,
                                       OrderStatusType orderStatusType) {

    public static ReadUserOrderAllResponse fromEntity(Order order, List<OrderBook> orderBooks) {
        List<Long> productIds = orderBooks.stream()
            .map(OrderBook::getBookId)
            .toList();

        List<Integer> quantities = orderBooks.stream()
            .map(OrderBook::getOrderProductQuantity)
            .toList();

        return ReadUserOrderAllResponse.builder()
            .orderId(order.getOrderId())
            .orderCreatedAt(LocalDate.from(order.getOrderCreatedAt()))
            .orderDeliveryAt(LocalDate.from(order.getOrderDeliveryAt()))
            .productIds(productIds)
            .amount(order.getOrderTotalAmount())
            .quantities(quantities)
            .orderStatusType(OrderStatusType.valueOf(order.getOrderStatus().getOrderStatusName()))
            .build();
    }
}
