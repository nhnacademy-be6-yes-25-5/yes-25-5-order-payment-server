package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.PaymentProcessor;
import com.yes25.yes255orderpaymentserver.presentation.dto.ApiResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentProcessor paymentService;
    private final OrderService orderService;

    @PostMapping("/confirm")
    public CreatePaymentResponse confirmPayment(@RequestBody CreatePaymentRequest request) {
        CreatePaymentResponse response = paymentService.createPayment(request);

        return ApiResponse.ok(response);
    }

    @GetMapping("/{orderId}")
    public List<ReadPaymentOrderResponse> findAll(@PathVariable String orderId) {
        return ApiResponse.ok(orderService.findAllOrderByOrderId(orderId));
    }

}
