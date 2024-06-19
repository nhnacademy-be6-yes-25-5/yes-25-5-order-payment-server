package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.PolicyService;
import com.yes25.yes255orderpaymentserver.presentation.dto.ApiResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadTakeoutResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/policies")
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping("/shipping")
    public ApiResponse<Page<ReadShippingPolicyResponse>> findAll(Pageable pageable) {
        return ApiResponse.ok(policyService.findAllShippingPolicy(pageable));
    }

    @GetMapping("/shipping/free")
    public ApiResponse<ReadShippingPolicyResponse> find() {
        return ApiResponse.ok(policyService.findFreeShippingPolicy());
    }

    @GetMapping("/takeout")
    public ApiResponse<List<ReadTakeoutResponse>> findAll() {
        return ApiResponse.ok(policyService.findAllTakeoutPolicy());
    }
}
