package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "order_customer_id")
    private Long customerId;

    @Column(name = "order_total_amount", nullable = false)
    private BigDecimal orderTotalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "takeout_id")
    private Takeout takeout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_status_id")
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private String addressRaw;

    private String addressDetail;

    @Column(nullable = false)
    private String zipCode;

    private LocalDateTime orderStartedAt;
    private LocalDateTime orderCreatedAt;

    @Column(nullable = false)
    private LocalDate orderDeliveryAt;

    @OneToMany(mappedBy = "order")
    private List<OrderBook> orderBooks = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal purePrice;

    private String orderUserName;
    private String orderUserEmail;
    private String orderUserPhoneNumber;
    private String receiveUserName;
    private String receiveUserEmail;
    private String receiveUserPhoneNumber;
    private String reference;
    private Long couponId;
    private BigDecimal points;


    @Builder
    public Order(String orderId, Long customerId, LocalDateTime orderStartedAt,
        BigDecimal orderTotalAmount,
        Takeout takeout, OrderStatus orderStatus, String addressRaw, String addressDetail,
        String zipCode, LocalDateTime orderCreatedAt, LocalDate orderDeliveryAt, List<OrderBook> orderBooks,
        BigDecimal purePrice, String orderUserName, String orderUserEmail, String orderUserPhoneNumber,
        String receiveUserName, String receiveUserEmail, String receiveUserPhoneNumber,
        String reference, Long couponId, BigDecimal points) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderStartedAt = orderStartedAt;
        this.orderTotalAmount = orderTotalAmount;
        this.takeout = takeout;
        this.orderStatus = orderStatus;
        this.addressRaw = addressRaw;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        this.orderCreatedAt = orderCreatedAt;
        this.orderDeliveryAt = orderDeliveryAt;
        this.orderBooks = orderBooks;
        this.purePrice = purePrice;
        this.orderUserName = orderUserName;
        this.orderUserEmail = orderUserEmail;
        this.orderUserPhoneNumber = orderUserPhoneNumber;
        this.receiveUserName = receiveUserName;
        this.receiveUserEmail = receiveUserEmail;
        this.receiveUserPhoneNumber = receiveUserPhoneNumber;
        this.reference = reference;
        this.couponId = couponId;
        this.points = points;
    }
}
