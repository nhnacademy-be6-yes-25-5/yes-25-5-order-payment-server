package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.PolicyService;
import com.yes25.yes255orderpaymentserver.common.jwt.HeaderUtils;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.common.jwt.annotation.CurrentUser;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadTakeoutResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/policies")
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "모든 배송 정책 조회", description = "페이징을 사용하여 모든 배송 정책을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/shipping")
    public ResponseEntity<Page<ReadShippingPolicyResponse>> findAll(Pageable pageable,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        if (Objects.isNull(jwtUserDetails)) {
            return ResponseEntity.ok(policyService.findAllShippingPolicy(pageable));
        }

        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(policyService.findAllShippingPolicy(pageable));
    }

    @Operation(summary = "무료 배송 정책 조회", description = "무료 배송 정책을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/shipping/free")
    public ResponseEntity<ReadShippingPolicyResponse> find(@CurrentUser JwtUserDetails jwtUserDetails) {
        if (Objects.isNull(jwtUserDetails)) {
            return ResponseEntity.ok(policyService.findFreeShippingPolicy());
        }

        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(policyService.findFreeShippingPolicy());
    }

    @Operation(summary = "모든 픽업 정책 조회", description = "모든 픽업 정책을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/takeout")
    public ResponseEntity<List<ReadTakeoutResponse>> findAll(@CurrentUser JwtUserDetails jwtUserDetails) {
        if (Objects.isNull(jwtUserDetails)) {
            return ResponseEntity.ok(policyService.findAllTakeoutPolicy());
        }

        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(policyService.findAllTakeoutPolicy());
    }
}
