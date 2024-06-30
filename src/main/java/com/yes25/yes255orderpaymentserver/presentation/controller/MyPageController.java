package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.MyPageOrderService;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.common.jwt.annotation.CurrentUser;
import com.yes25.yes255orderpaymentserver.presentation.dto.ApiResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadMyOrderHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/orders")
@RequiredArgsConstructor
@RestController
public class MyPageController {

    private final MyPageOrderService myPageOrderService;

    @GetMapping("/mypage")
    public Page<ReadMyOrderHistoryResponse> getMyOrders(Pageable pageable, @CurrentUser JwtUserDetails jwtUserDetails) {
        return ApiResponse.ok(myPageOrderService.getMyOrdersByPaging(pageable, jwtUserDetails.userId()));
    }
}
