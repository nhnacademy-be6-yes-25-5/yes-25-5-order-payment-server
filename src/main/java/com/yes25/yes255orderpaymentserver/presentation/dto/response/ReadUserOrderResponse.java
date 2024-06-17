package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ReadUserOrderResponse(String orderId,
                                    List<Long> productIds,
                                    LocalDateTime deliveryDate,
                                    String receiveUserName,
                                    String receiveUserPhoneNumber,
                                    String orderAddress,
                                    String reference,
                                    BigDecimal amount) {

    public static ReadUserOrderResponse fromEntities(Order order, List<OrderBook> orderBooks) {
        List<Long> productIds = orderBooks.stream()
            .map(OrderBook::getBookId)
            .toList();

        return ReadUserOrderResponse.builder()
            .orderId(order.getOrderId())
            .productIds(productIds)
            .orderAddress(order.getAddressRaw() + " " + order.getAddressDetail())
            .deliveryDate(order.getOrderDeliveryAt())
            .receiveUserName(order.getOrderUserName())
            .receiveUserPhoneNumber(order.getReceiveUserPhoneNumber())
            .reference(order.getReference())
            .amount(order.getOrderTotalAmount())
            .build();
    }
}
