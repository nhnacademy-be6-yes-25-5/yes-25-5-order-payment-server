package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.service.PolicyService;
import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import com.yes25.yes255orderpaymentserver.persistance.repository.ShippingPolicyRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyAllResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final ShippingPolicyRepository shippingPolicyRepository;

    @Override
    public Page<ReadShippingPolicyAllResponse> findAllShippingPolicy(Pageable pageable) {
        Page<ShippingPolicy> shippingPolicies = shippingPolicyRepository.findAll(pageable);

        List<ReadShippingPolicyAllResponse> responses = shippingPolicies.stream()
            .map(ReadShippingPolicyAllResponse::fromEntity)
            .toList();

        return new PageImpl<>(responses, pageable, shippingPolicies.getTotalElements());
    }
}
