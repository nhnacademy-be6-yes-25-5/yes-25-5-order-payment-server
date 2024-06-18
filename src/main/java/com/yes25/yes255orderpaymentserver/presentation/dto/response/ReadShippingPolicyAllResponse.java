package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ReadShippingPolicyAllResponse(Long shippingPolicyId,
                                            BigDecimal shippingPolicyFee,
                                            BigDecimal shippingPolicyMinAmount) {

    public static ReadShippingPolicyAllResponse fromEntity(ShippingPolicy shippingPolicy) {
        return ReadShippingPolicyAllResponse.builder()
            .shippingPolicyId(shippingPolicy.getShippingPolicyId())
            .shippingPolicyFee(shippingPolicy.getShippingPolicyFee())
            .shippingPolicyMinAmount(shippingPolicy.getShippingPolicyMinAmount())
            .build();
    }
}
