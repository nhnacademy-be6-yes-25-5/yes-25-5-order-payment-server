package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shippingPolicyId;

    @Column(nullable = false)
    private BigDecimal shippingPolicyFee;

    @Column(nullable = false)
    private BigDecimal shippingPolicyMinAmount;

    @Column(nullable = false)
    private Boolean shippingPolicyIsMemberOnly;

}