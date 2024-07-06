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
import java.util.List;
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

    @GetMapping("/admin")
    public Page<ReadAllOrderResponse> getAllOrders(Pageable pageable,
        @RequestParam(required = false) String role) {
        return adminOrderService.getAllOrdersByPaging(pageable, role);
    }

    @PutMapping("/admin/{orderId}")
    public void update(@PathVariable String orderId,
        @RequestBody UpdateOrderStatusRequest request) {
        adminOrderService.updateOrderStatusByOrderId(orderId, request);
    }

    @GetMapping("/admin/refund")
    public ResponseEntity<Page<ReadAllUserOrderCancelStatusResponse>> getAllOrders(
        Pageable pageable,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(adminOrderService.getAllCancelOrdersByPaging(pageable));
    }

    @PutMapping("/admin/{orderId}/refund")
    public ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable String orderId,
        @RequestBody CancelOrderRequest request,
        @CurrentUser JwtUserDetails jwtUserDetails) {

        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(adminOrderService.cancelOrderByOrderId(orderId, request));
    }
}
