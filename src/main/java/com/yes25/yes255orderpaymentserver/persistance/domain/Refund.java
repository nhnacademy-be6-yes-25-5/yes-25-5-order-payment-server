package com.yes25.yes255orderpaymentserver.persistance.domain;

import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @Column(nullable = false)
    private LocalDate requestedAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_status_id")
    private RefundStatus refundStatus;

    @Builder
    public Refund(Long refundId, LocalDate requestedAt, LocalDateTime updatedAt, Order order, RefundStatus refundStatus) {
        this.refundId = refundId;
        this.requestedAt = requestedAt;
        this.updatedAt = updatedAt;
        this.order = order;
        this.refundStatus = refundStatus;
    }

    public static Refund toEntity(Order order, RefundStatus refundStatus) {
        return Refund.builder()
            .requestedAt(LocalDate.now())
            .order(order)
            .refundStatus(refundStatus)
            .build();
    }

    public void updateRefundStatusAndUpdatedAt(RefundStatus refundStatus, LocalDateTime now) {
        this.refundStatus = refundStatus;
        this.updatedAt = now;
    }
}
