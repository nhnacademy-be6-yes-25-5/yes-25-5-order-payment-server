package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.AdminOrderService;
import com.yes25.yes255orderpaymentserver.common.jwt.HeaderUtils;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.common.jwt.annotation.CurrentUser;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CancelOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderStatusRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CancelOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllUserOrderCancelStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "모든 주문 조회", description = "페이징을 사용하여 모든 주문을 조회합니다. 역할(role)을 필터링할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/admin")
    public ResponseEntity<Page<ReadAllOrderResponse>> getAllOrders(Pageable pageable,
        @RequestParam(required = false) String role) {
        return ResponseEntity.ok(adminOrderService.getAllOrdersByPaging(pageable, role));
    }

    @Operation(summary = "주문 상태 업데이트", description = "주문 ID로 주문 상태를 업데이트합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "업데이트 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/admin/{orderId}")
    public ResponseEntity<Void> update(@PathVariable String orderId,
        @RequestBody UpdateOrderStatusRequest request,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        adminOrderService.updateOrderStatusByOrderId(orderId, request);

        return ResponseEntity.noContent()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .build();
    }

    @Operation(summary = "모든 환불 주문 조회", description = "페이징을 사용하여 모든 환불 주문을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/admin/refund")
    public ResponseEntity<Page<ReadAllUserOrderCancelStatusResponse>> getAllOrders(
        Pageable pageable,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(adminOrderService.getAllCancelOrdersByPaging(pageable));
    }

    @Operation(summary = "주문 환불 처리", description = "주문 ID로 주문 환불을 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "환불 처리 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/admin/{orderId}/refund")
    public ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable String orderId,
        @RequestBody CancelOrderRequest request,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(adminOrderService.cancelOrderByOrderId(orderId, request));
    }
}
