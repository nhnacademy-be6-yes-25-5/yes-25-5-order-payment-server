package com.yes25.yes255orderpaymentserver.persistance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RefundStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundStatusId;

    @Column(nullable = false)
    private String refundStatusName;

    @Builder
    public RefundStatus(Long refundStatusId, String refundStatusName) {
        this.refundStatusId = refundStatusId;
        this.refundStatusName = refundStatusName;
    }
}
