package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ReadOrderDetailResponse(
    String orderId,
    Long customerId,
    BigDecimal orderTotalAmount,
    OrderStatusType orderStatusType,
    String addressRaw,
    String addressDetail,
    String zipCode,
    Integer shippingPrice,
    Integer bookTotalPrice,
    LocalDateTime orderCreatedAt,
    LocalDateTime deliveryStartedAt,
    LocalDate orderDeliveryAt,
    BigDecimal purePrice,
    String orderUserName,
    String orderUserEmail,
    String orderUserPhoneNumber,
    String receiveUserName,
    String receiveUserEmail,
    String receiveUserPhoneNumber,
    String userRole,
    String reference,
    Long couponId,
    BigDecimal points,
    List<String> bookNames,
    List<Integer> quantities,
    List<BigDecimal> bookPrices,
    CancelStatus cancelStatus,
    PaymentProvider paymentProvider) {

    public static ReadOrderDetailResponse of(Order order, List<ReadBookResponse> responses,
        List<OrderBook> orderBooks) {
        return buildResponse(order, responses, orderBooks, CancelStatus.NONE);
    }

    public static ReadOrderDetailResponse of(Order order, List<ReadBookResponse> responses,
        List<OrderBook> orderBooks, Refund refund) {
        return buildResponse(order, responses, orderBooks,
            CancelStatus.valueOf(refund.getRefundStatus().getRefundStatusName()));
    }

    private static ReadOrderDetailResponse buildResponse(Order order, List<ReadBookResponse> responses,
        List<OrderBook> orderBooks, CancelStatus cancelStatus) {
        List<String> bookNames = responses.stream()
            .map(ReadBookResponse::bookName)
            .toList();

        List<Integer> quantities = orderBooks.stream()
            .map(OrderBook::getOrderBookQuantity)
            .toList();

        List<BigDecimal> bookPrices = orderBooks.stream()
            .map(OrderBook::getPrice)
            .toList();

        Integer bookTotalPrice = responses.stream()
            .map(ReadBookResponse::bookPrice)
            .map(BigDecimal::intValue)
            .reduce(0, Integer::sum);

        return ReadOrderDetailResponse.builder()
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .orderTotalAmount(order.getOrderTotalAmount())
            .orderStatusType(OrderStatusType.valueOf(order.getOrderStatus().getOrderStatusName()))
            .addressRaw(order.getAddressRaw())
            .addressDetail(order.getAddressDetail())
            .zipCode(order.getZipCode())
            .orderCreatedAt(order.getOrderCreatedAt())
            .deliveryStartedAt(order.getDeliveryStartedAt())
            .orderDeliveryAt(order.getOrderDeliveryAt())
            .purePrice(order.getPurePrice())
            .orderUserName(order.getOrderUserName())
            .orderUserEmail(order.getOrderUserEmail())
            .orderUserPhoneNumber(order.getOrderUserPhoneNumber())
            .receiveUserName(order.getReceiveUserName())
            .receiveUserEmail(order.getReceiveUserEmail())
            .receiveUserPhoneNumber(order.getReceiveUserPhoneNumber())
            .userRole(order.getUserRole())
            .reference(order.getReference())
            .couponId(order.getCouponId())
            .points(order.getPoints())
            .bookNames(bookNames)
            .quantities(quantities)
            .shippingPrice(order.getTakeout().getTakeoutPrice().intValue())
            .bookTotalPrice(bookTotalPrice)
            .bookPrices(bookPrices)
            .cancelStatus(cancelStatus)
            .paymentProvider(PaymentProvider.from(order.getPayment().getPaymentProvider()))
            .build();
    }
}
