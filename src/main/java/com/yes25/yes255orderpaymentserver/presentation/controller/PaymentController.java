package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.PaymentProcessor;
import com.yes25.yes255orderpaymentserver.common.jwt.HeaderUtils;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.common.jwt.annotation.CurrentUser;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentProcessor paymentService;
    private final OrderService orderService;

    @Operation(summary = "결제 확인", description = "결제를 확인하고 결제 정보를 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "결제 확인 성공", content = @Content(schema = @Schema(implementation = CreatePaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/confirm")
    public ResponseEntity<CreatePaymentResponse> confirmPayment(@RequestBody CreatePaymentRequest request,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        if (Objects.isNull(jwtUserDetails)) {
            return ResponseEntity.ok(paymentService.createPayment(request));
        }

        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(paymentService.createPayment(request));
    }

    @Operation(summary = "주문별 결제 내역 조회", description = "주문 ID로 모든 결제 내역을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "결제 내역을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<List<ReadPaymentOrderResponse>> findAll(@PathVariable String orderId,
    @CurrentUser JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(orderService.findAllOrderByOrderId(orderId));
    }

    @Operation(summary = "0원 결제 확인", description = "0원 결제를 확인하고 결제 정보를 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "결제 확인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/confirm/zero")
    public ResponseEntity<CreatePaymentResponse> confirmPaymentByZero(@RequestBody CreatePaymentRequest request,
        @CurrentUser JwtUserDetails jwtUserDetails) {
        if (Objects.isNull(jwtUserDetails)) {
            return ResponseEntity.ok(paymentService.createPaymentByZeroAmount(request));
        }

        return ResponseEntity.ok()
            .headers(HeaderUtils.addAuthHeaders(jwtUserDetails))
            .body(paymentService.createPaymentByZeroAmount(request));
    }
}
