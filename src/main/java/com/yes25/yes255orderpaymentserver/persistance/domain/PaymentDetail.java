package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentDetailId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private BigDecimal paymentAmount;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Column(nullable = false)
    private LocalDateTime approveAt;

    @Column(nullable = false)
    private String paymentMethod;

    @Builder
    public PaymentDetail(Long paymentDetailId, Payment payment, BigDecimal paymentAmount,
        LocalDateTime requestedAt,
        LocalDateTime approveAt, String paymentMethod) {
        this.paymentDetailId = paymentDetailId;
        this.payment = payment;
        this.paymentAmount = paymentAmount;
        this.requestedAt = requestedAt;
        this.approveAt = approveAt;
        this.paymentMethod = paymentMethod;
    }


    public static PaymentDetail from(JSONObject jsonObject, Payment payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime approvedAt = LocalDateTime.parse((String) jsonObject.get("approvedAt"), formatter);
        LocalDateTime requestedAt = LocalDateTime.parse((String) jsonObject.get("requestedAt"), formatter);

        return PaymentDetail.builder()
            .paymentAmount(BigDecimal.valueOf((Integer) jsonObject.get("totalAmount")))
            .approveAt(approvedAt)
            .requestedAt(requestedAt)
            .paymentMethod((String) jsonObject.get("method"))
            .payment(payment)
            .build();
    }
}
