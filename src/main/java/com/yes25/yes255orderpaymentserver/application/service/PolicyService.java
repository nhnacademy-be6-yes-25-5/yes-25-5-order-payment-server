package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadTakeoutResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyService {

    Page<ReadShippingPolicyResponse> findAllShippingPolicy(Pageable pageable);

    ReadShippingPolicyResponse findFreeShippingPolicy();

    List<ReadTakeoutResponse> findAllTakeoutPolicy();
}
