package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.queue.OrderProducer;
import com.yes25.yes255orderpaymentserver.presentation.dto.ApiResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/orders")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderProducer orderProducer;
    private final OrderService orderService;

    @PostMapping
    public CreateOrderResponse create(
        @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderProducer.sendCreateOrder(request));
    }

    @GetMapping("/users/{userId}")
    public Page<ReadUserOrderAllResponse> findAllOrderByUserId(
        Pageable pageable,
        @PathVariable Long userId) {
        return ApiResponse.ok(orderService.findByUserId(userId, pageable));
    }

    @GetMapping("/{orderId}/users/{userId}")
    public ReadUserOrderResponse findByOrderIdAndUserId(@PathVariable String orderId,
        @PathVariable Long userId) {
        return ApiResponse.ok(orderService.findByOrderIdAndUserId(orderId, userId));
    }

    @GetMapping("/{orderId}")
    public List<ReadPaymentOrderResponse> findAll(@PathVariable String orderId) {
        return ApiResponse.ok(orderService.findAllOrderByOrderId(orderId));
    }

    @GetMapping("/status/{orderId}")
    public ReadOrderStatusResponse find(@PathVariable String orderId) {
        return ApiResponse.ok(orderService.findOrderStatusByOrderId(orderId));
    }
}
