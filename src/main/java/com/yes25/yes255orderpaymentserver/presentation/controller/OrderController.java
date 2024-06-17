package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.queue.OrderProducer;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CreateOrderResponse> createFakeOrder(
        @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderProducer.sendCreateOrder(request));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<ReadUserOrderResponse>> findOrderByUserId(
        Pageable pageable,
        @PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId, pageable));
    }

}
