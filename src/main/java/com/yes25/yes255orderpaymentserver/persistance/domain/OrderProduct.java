package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderProduct extends Time{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderProductId;
    private Long bookId;
    private Integer orderProductQuantity;
    private BigDecimal price;

    @ManyToOne
    private Order order;

    @Builder
    public OrderProduct(Long orderProductId, Long bookId, Integer orderProductQuantity,
        BigDecimal price, Order order) {
        this.orderProductId = orderProductId;
        this.bookId = bookId;
        this.orderProductQuantity = orderProductQuantity;
        this.price = price;
        this.order = order;
    }
}
