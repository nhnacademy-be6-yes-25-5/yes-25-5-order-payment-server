package com.yes25.yes255orderpaymentserver.persistance.repository;

import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingPolicyRepository extends JpaRepository<ShippingPolicy, Long> {

    ShippingPolicy findByShippingPolicyFee(BigDecimal zero);
}
