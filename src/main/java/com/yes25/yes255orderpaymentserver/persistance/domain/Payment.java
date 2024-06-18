package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private BigDecimal paymentAmount;
    private LocalDateTime requestedAt;
    private LocalDateTime approveAt;
    private String paymentMethod;
    private String orderId;

    @Builder
    public Payment(Long paymentId, BigDecimal paymentAmount, LocalDateTime requestedAt,
        LocalDateTime approveAt,
        String paymentMethod, String orderId) {
        this.paymentId = paymentId;
        this.paymentAmount = paymentAmount;
        this.requestedAt = requestedAt;
        this.approveAt = approveAt;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
    }

    public static Payment from(JSONObject jsonObject) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime approvedAt = LocalDateTime.parse((String) jsonObject.get("approvedAt"), formatter);
        LocalDateTime requestedAt = LocalDateTime.parse((String) jsonObject.get("requestedAt"), formatter);

        return Payment.builder()
            .paymentAmount(BigDecimal.valueOf((Long) jsonObject.get("totalAmount")))
            .approveAt(approvedAt)
            .requestedAt(requestedAt)
            .paymentMethod((String) jsonObject.get("method"))
            .orderId((String) jsonObject.get("orderId"))
            .build();
    }
}
