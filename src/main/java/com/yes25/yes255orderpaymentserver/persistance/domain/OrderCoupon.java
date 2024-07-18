package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderCouponId;

    @Column(nullable = false)
    private Long userCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    public OrderCoupon(Long orderCouponId, Long userCouponId, Order order) {
        this.orderCouponId = orderCouponId;
        this.userCouponId = userCouponId;
        this.order = order;
    }
}
