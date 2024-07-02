package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadPurePriceResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.common.jwt.annotation.CurrentUser;
import com.yes25.yes255orderpaymentserver.presentation.dto.ApiResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDeliveryResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDetailResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/orders")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final MessageProducer messageProducer;
    private final OrderService orderService;

    @PostMapping
    public CreateOrderResponse create(
        @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(messageProducer.sendCreateOrder(request));
    }

    @GetMapping("/users")
    public Page<ReadUserOrderAllResponse> findAllOrderByUserId(
        Pageable pageable
        ,@CurrentUser JwtUserDetails jwtUserDetails
    ) {
        Long userId = jwtUserDetails.userId();
        return ApiResponse.ok(orderService.findByUserId(userId, pageable));
    }

    @GetMapping("/{orderId}/users")
    public ReadUserOrderResponse findByOrderIdAndUserId(@PathVariable String orderId
    , @CurrentUser JwtUserDetails jwtUserDetails
    ) {
        Long userId = jwtUserDetails.userId();
        return ApiResponse.ok(orderService.findByOrderIdAndUserId(orderId, userId));
    }

    @GetMapping("/status/{orderId}")
    public ReadOrderStatusResponse find(@PathVariable String orderId) {
        return ApiResponse.ok(orderService.findOrderStatusByOrderId(orderId));
    }

    @PutMapping("/{orderId}")
    public UpdateOrderResponse update(@PathVariable String orderId,
        @RequestBody UpdateOrderRequest request,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        return ApiResponse.ok(orderService.updateOrderStatusByOrderId(orderId, request, jwtUserDetails.userId()));
    }

    @GetMapping("/{orderId}/delivery")
    public ReadOrderDeliveryResponse getDelivery(@PathVariable String orderId) {
        return ApiResponse.ok(orderService.getByOrderIdAndUserId(orderId));
    }

    @GetMapping("/{orderId}")
    public ReadOrderDetailResponse getOrder(@PathVariable String orderId,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        return ApiResponse.ok(orderService.getOrderByOrderId(orderId, jwtUserDetails.userId()));
    }

    @GetMapping("/logs")
    public List<ReadPurePriceResponse> getPurePrices(@RequestParam LocalDate date) {
        return ApiResponse.ok(orderService.getPurePriceByDate(date));
    }

    @GetMapping("/none/{orderId}")
    public ReadOrderDetailResponse getOrderNoneMember(@PathVariable String orderId, @RequestParam String email) {
        return ApiResponse.ok(orderService.getOrderByOrderIdAndEmail(orderId, email));
    }
}
