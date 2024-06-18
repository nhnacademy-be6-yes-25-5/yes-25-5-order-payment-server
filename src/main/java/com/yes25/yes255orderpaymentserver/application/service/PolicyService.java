package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyAllResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyService {

    Page<ReadShippingPolicyAllResponse> findAllShippingPolicy(Pageable pageable);
}
