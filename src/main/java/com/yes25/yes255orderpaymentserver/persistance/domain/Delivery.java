package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String deliveryStatus;

    @Builder
    public Delivery(Long deliveryId, Order order, LocalDateTime timestamp, String deliveryStatus) {
        this.deliveryId = deliveryId;
        this.order = order;
        this.timestamp = timestamp;
        this.deliveryStatus = deliveryStatus;
    }

    public static Delivery toEntity(Order order) {
        return Delivery.builder()
            .order(order)
            .timestamp(LocalDateTime.now())
            .deliveryStatus(order.getOrderStatus().getOrderStatusName())
            .build();
    }

    public void addOrder(Order order) {
        this.order = order;
    }
}
