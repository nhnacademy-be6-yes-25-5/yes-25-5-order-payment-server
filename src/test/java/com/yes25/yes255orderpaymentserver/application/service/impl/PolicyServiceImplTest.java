package com.yes25.yes255orderpaymentserver.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.repository.ShippingPolicyRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.TakeoutRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadTakeoutResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private ShippingPolicyRepository shippingPolicyRepository;

    @Mock
    private TakeoutRepository takeoutRepository;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private ShippingPolicy shippingPolicy;
    private ShippingPolicy freePolicy;
    private Takeout takeout;

    @BeforeEach
    void setUp() {
        shippingPolicy = ShippingPolicy.builder()
            .shippingPolicyId(1L)
            .shippingPolicyMinAmount(BigDecimal.valueOf(30000))
            .shippingPolicyIsMemberOnly(false)
            .shippingPolicyFee(BigDecimal.valueOf(10000))
            .shippingPolicyIsReturnPolicy(false)
            .build();

        freePolicy = ShippingPolicy.builder()
            .shippingPolicyId(2L)
            .shippingPolicyMinAmount(BigDecimal.valueOf(30000))
            .shippingPolicyIsMemberOnly(true)
            .shippingPolicyFee(BigDecimal.ZERO)
            .shippingPolicyIsReturnPolicy(false)
            .build();

        takeout = Takeout.builder()
            .takeoutId(1L)
            .takeoutName("NONE")
            .takeoutDescription("포장 없음")
            .takeoutPrice(BigDecimal.valueOf(1000))
            .build();
    }

    @DisplayName("모든 배송 정책을 페이징 처리하여 가져오는지 확인한다.")
    @Test
    void findAllShippingPolicy() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ShippingPolicy> shippingPolicies = new PageImpl<>(List.of(shippingPolicy), pageable, 1);
        when(shippingPolicyRepository.findAllByShippingPolicyIsReturnPolicyFalse(pageable)).thenReturn(shippingPolicies);

        // when
        Page<ReadShippingPolicyResponse> responses = policyService.findAllShippingPolicy(pageable);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(1);
    }

    @DisplayName("무료 배송 정책을 가져오는지 확인한다.")
    @Test
    void findFreeShippingPolicy() {
        // given
        when(shippingPolicyRepository.findByShippingPolicyFeeAndShippingPolicyIsReturnPolicyFalse(BigDecimal.ZERO)).thenReturn(Optional.of(freePolicy));

        // when
        ReadShippingPolicyResponse response = policyService.findFreeShippingPolicy();

        // then
        assertThat(response).isNotNull();
        assertThat(response.shippingPolicyFee()).isEqualTo(0);
    }

    @DisplayName("무료 배송 정책을 찾지 못할 때 예외를 던지는지 확인한다.")
    @Test
    void findFreeShippingPolicy_NotFound() {
        // given
        when(shippingPolicyRepository.findByShippingPolicyFeeAndShippingPolicyIsReturnPolicyFalse(BigDecimal.ZERO)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> policyService.findFreeShippingPolicy());
    }

    @DisplayName("모든 포장 정책을 가져오는지 확인한다.")
    @Test
    void findAllTakeoutPolicy() {
        // given
        when(takeoutRepository.findAll()).thenReturn(List.of(takeout));

        // when
        List<ReadTakeoutResponse> responses = policyService.findAllTakeoutPolicy();

        // then
        assertThat(responses).isNotNull();
    }
}
