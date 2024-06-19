package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import lombok.Builder;

@Builder
public record ReadShippingPolicyResponse(Long shippingPolicyId,
                                         Integer shippingPolicyFee,
                                         Integer shippingPolicyMinAmount) {

    public static ReadShippingPolicyResponse fromEntity(ShippingPolicy shippingPolicy) {
        return ReadShippingPolicyResponse.builder()
            .shippingPolicyId(shippingPolicy.getShippingPolicyId())
            .shippingPolicyFee(shippingPolicy.getShippingPolicyFee().intValue())
            .shippingPolicyMinAmount(shippingPolicy.getShippingPolicyMinAmount().intValue())
            .build();
    }
}
