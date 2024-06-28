package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderBookId;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private Integer orderBookQuantity;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder
    public OrderBook(Long orderBookId, Long bookId, Integer orderBookQuantity,
        BigDecimal price, Order order) {
        this.orderBookId = orderBookId;
        this.bookId = bookId;
        this.orderBookQuantity = orderBookQuantity;
        this.price = price;
        this.order = order;
    }
}
