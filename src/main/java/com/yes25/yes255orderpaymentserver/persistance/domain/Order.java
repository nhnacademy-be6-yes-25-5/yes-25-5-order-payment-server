package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_customer_id")
    private Long customerId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "order_total_amount", nullable = false)
    private BigDecimal orderTotalAmount;

    @Column(name = "order_is_takeout", nullable = false)
    private Boolean orderIsTakeout;

    @Column(name = "order_crated_at", nullable = false)
    private LocalDateTime orderCreatedAt;

    @Column(name = "order_updated_at")
    private LocalDateTime orderUpdatedAt;

    @Column(name = "wrapping_option_id")
    private Long wrappingOptionId;

    @Column(name = "order_status_id", nullable = false)
    private Long orderStatusId;

    @Column(name = "address_raw", nullable = false)
    private String addressRaw;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;

    @Column(name = "order_started_at")
    private LocalDateTime orderStartedAt;

    @Column(name = "order_delivery_date", nullable = false)
    private LocalDateTime orderDeliveryDate;

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Builder
    public Order(Long orderId, Long customerId, LocalDateTime orderDate,
        BigDecimal orderTotalAmount,
        Boolean orderIsTakeout, LocalDateTime orderCreatedAt, LocalDateTime orderUpdatedAt,
        Long wrappingOptionId, Long orderStatusId, String addressRaw, String addressDetail,
        String zipCode, LocalDateTime orderStartedAt, LocalDateTime orderDeliveryDate,
        List<OrderProduct> orderProducts) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.orderTotalAmount = orderTotalAmount;
        this.orderIsTakeout = orderIsTakeout;
        this.orderCreatedAt = orderCreatedAt;
        this.orderUpdatedAt = orderUpdatedAt;
        this.wrappingOptionId = wrappingOptionId;
        this.orderStatusId = orderStatusId;
        this.addressRaw = addressRaw;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        this.orderStartedAt = orderStartedAt;
        this.orderDeliveryDate = orderDeliveryDate;
        this.orderProducts = orderProducts;
    }
}
