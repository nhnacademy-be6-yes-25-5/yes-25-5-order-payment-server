package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.service.PolicyService;
import com.yes25.yes255orderpaymentserver.common.exception.PolicyNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.repository.ShippingPolicyRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.TakeoutRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadTakeoutResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final TakeoutRepository takeoutRepository;

    @Override
    public Page<ReadShippingPolicyResponse> findAllShippingPolicy(Pageable pageable) {
        Page<ShippingPolicy> shippingPolicies = shippingPolicyRepository.findAllByShippingPolicyIsReturnPolicyFalse(pageable);

        List<ReadShippingPolicyResponse> responses = shippingPolicies.stream()
            .map(ReadShippingPolicyResponse::fromEntity)
            .toList();

        return new PageImpl<>(responses, pageable, shippingPolicies.getTotalElements());
    }

    @Override
    public ReadShippingPolicyResponse findFreeShippingPolicy() {
        ShippingPolicy shippingPolicy = shippingPolicyRepository.findByShippingPolicyFeeAndShippingPolicyIsReturnPolicyFalse(BigDecimal.ZERO)
            .orElseThrow(() -> new PolicyNotFoundException(
                ErrorStatus.toErrorStatus("무료 배송 정책을 찾을 수 없습니다.", 404, LocalDateTime.now())
            ));

        return ReadShippingPolicyResponse.fromEntity(shippingPolicy);
    }

    @Override
    public List<ReadTakeoutResponse> findAllTakeoutPolicy() {
        List<Takeout> takeouts =  takeoutRepository.findAll();

        return takeouts.stream()
            .map(ReadTakeoutResponse::fromEntity)
            .toList();
    }
}
