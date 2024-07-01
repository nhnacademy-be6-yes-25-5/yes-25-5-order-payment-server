package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

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
    private BigDecimal paymentAmount;

    @Column(nullable = false)
    private LocalDateTime approveAt;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String preOrderId;

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private Order order;

    @Builder
    public Payment(Long paymentId, String paymentKey, BigDecimal paymentAmount,
        LocalDateTime approveAt,
        LocalDateTime requestedAt, String paymentMethod, String preOrderId, Order order) {
        this.paymentId = paymentId;
        this.paymentKey = paymentKey;
        this.paymentAmount = paymentAmount;
        this.approveAt = approveAt;
        this.requestedAt = requestedAt;
        this.paymentMethod = paymentMethod;
        this.preOrderId = preOrderId;
        this.order = order;
    }

    public static Payment from(JSONObject jsonObject) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime approvedAt = LocalDateTime.parse((String) jsonObject.get("approvedAt"), formatter);
        LocalDateTime requestedAt = LocalDateTime.parse((String) jsonObject.get("requestedAt"), formatter);

        return Payment.builder()
            .paymentAmount(BigDecimal.valueOf((Long) jsonObject.get("totalAmount")))
            .paymentKey((String) jsonObject.get("paymentKey"))
            .approveAt(approvedAt)
            .requestedAt(requestedAt)
            .paymentMethod((String) jsonObject.get("method"))
            .preOrderId((String) jsonObject.get("orderId"))
            .build();
    }

    public void addOrder(Order savedOrder) {
        this.order = savedOrder;
    }
}
