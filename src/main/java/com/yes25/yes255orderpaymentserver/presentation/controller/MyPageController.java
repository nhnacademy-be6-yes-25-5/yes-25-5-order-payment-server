package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.MyPageOrderService;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.common.jwt.annotation.CurrentUser;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadMyOrderHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/orders")
@RequiredArgsConstructor
@RestController
public class MyPageController {

    private final MyPageOrderService myPageOrderService;

    @Operation(summary = "마이 페이지 주문 내역 조회", description = "페이징을 사용하여 사용자의 주문 내역을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/mypage")
    public ResponseEntity<Page<ReadMyOrderHistoryResponse>> getMyOrders(Pageable pageable, @CurrentUser JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok(myPageOrderService.getMyOrdersByPaging(pageable, jwtUserDetails.userId()));
    }
}
