package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyService {

    Page<ReadShippingPolicyResponse> findAllShippingPolicy(Pageable pageable);

    ReadShippingPolicyResponse findFreeShippingPolicy();
}
