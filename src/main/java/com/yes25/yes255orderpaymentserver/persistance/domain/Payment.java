package com.yes25.yes255orderpaymentserver.persistance.domain;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false)
    private String paymentKey;

    @Column(nullable = false)
    private String preOrderId;

    @Column(nullable = false)
    private String paymentProvider;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
    private PaymentDetail paymentDetail;

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private Order order;

    @Builder
    public Payment(Long paymentId, String paymentKey, String preOrderId, String paymentProvider,
        PaymentDetail paymentDetail, Order order) {
        this.paymentId = paymentId;
        this.paymentKey = paymentKey;
        this.preOrderId = preOrderId;
        this.paymentProvider = paymentProvider;
        this.paymentDetail = paymentDetail;
        this.order = order;
    }

    public static Payment from(PaymentProvider paymentProvider, String paymentKey) {
        return Payment.builder()
            .paymentKey(paymentKey)
            .paymentProvider(paymentProvider.name().toLowerCase())
            .build();
    }

    public void addOrder(Order savedOrder) {
        this.order = savedOrder;
    }
}
