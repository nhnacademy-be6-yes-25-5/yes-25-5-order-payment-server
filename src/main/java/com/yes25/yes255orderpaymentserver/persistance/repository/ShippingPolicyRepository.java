package com.yes25.yes255orderpaymentserver.persistance.repository;

import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingPolicyRepository extends JpaRepository<ShippingPolicy, Long> {

    Optional<ShippingPolicy> findByShippingPolicyFeeAndShippingPolicyIsRefundPolicyFalse(BigDecimal zero);

    Page<ShippingPolicy> findAllByShippingPolicyIsRefundPolicyFalse(Pageable pageable);

    Optional<ShippingPolicy> findByShippingPolicyIsRefundPolicyTrue();
}
