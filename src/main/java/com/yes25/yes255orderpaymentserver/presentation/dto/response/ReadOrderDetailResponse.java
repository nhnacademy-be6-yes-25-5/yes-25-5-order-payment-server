package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.persistance.domain.Delivery;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ReadOrderDetailResponse(String orderId, Long customerId, BigDecimal orderTotalAmount,
                                      LocalDateTime orderCreatedAt, OrderStatusType orderStatusType,
                                      String addressRaw, String addressDetail, String zipCode, String reference,
                                      String orderUserName, String orderUserEmail,
                                      String orderUserPhoneNumber, String receiveUserName,
                                      String receiveUserEmail, String receiveUserPhoneNumber,
                                      List<String> bookNames,
                                      List<String> bookAuthors,
                                      List<Integer> bookPrices,
                                      List<LocalDateTime> timestamp,
                                      List<String> deliveryStatuses) {

    public static ReadOrderDetailResponse of(Order order, List<ReadBookResponse> responses,
        List<Delivery> deliveries) {
        List<String> bookNames = responses.stream()
            .map(ReadBookResponse::bookName)
            .toList();

        List<String> bookAuthors = responses.stream()
            .map(ReadBookResponse::bookName)
            .toList();

        List<Integer> bookPrices = responses.stream()
            .map(ReadBookResponse::bookPrice)
            .map(BigDecimal::intValue)
            .toList();

        List<LocalDateTime> timestamp = deliveries.stream()
            .map(Delivery::getTimestamp)
            .toList();

        List<String> deliveryStatuses = deliveries.stream()
            .map(Delivery::getDeliveryStatus)
            .toList();

        return ReadOrderDetailResponse.builder()
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .orderTotalAmount(order.getOrderTotalAmount())
            .orderCreatedAt(order.getOrderCreatedAt())
            .orderStatusType(OrderStatusType.valueOf(order.getOrderStatus().getOrderStatusName()))
            .addressRaw(order.getAddressRaw())
            .addressDetail(order.getAddressDetail())
            .zipCode(order.getZipCode())
            .reference(order.getReference() != null ? order.getReference() : "")
            .orderUserName(order.getOrderUserName())
            .orderUserEmail(order.getOrderUserEmail())
            .orderUserPhoneNumber(order.getOrderUserPhoneNumber())
            .receiveUserName(order.getReceiveUserName())
            .receiveUserEmail(order.getReceiveUserEmail())
            .receiveUserPhoneNumber(order.getReceiveUserPhoneNumber())
            .bookNames(bookNames)
            .bookAuthors(bookAuthors)
            .bookPrices(bookPrices)
            .timestamp(timestamp)
            .deliveryStatuses(deliveryStatuses)
            .build();
    }
}
