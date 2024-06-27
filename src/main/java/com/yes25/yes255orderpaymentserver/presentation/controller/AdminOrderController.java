package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.AdminOrderService;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping("/admin")
    public Page<ReadAllOrderResponse> getAllOrders(Pageable pageable) {
        return adminOrderService.getAllOrdersByPaging(pageable);
    }
}
