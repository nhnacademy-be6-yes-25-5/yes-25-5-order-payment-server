package com.yes25.yes255orderpaymentserver.presentation.controller;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadPurePriceResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.common.jwt.annotation.CurrentUser;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDeliveryResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDetailResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주문 생성 성공", content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(messageProducer.sendCreateOrder(request));
    }

    @Operation(summary = "사용자 주문 내역 조회", description = "사용자의 모든 주문 내역을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "404", description = "주문 내역을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/users")
    public ResponseEntity<Page<ReadUserOrderAllResponse>> findAllOrderByUserId(Pageable pageable, @CurrentUser JwtUserDetails jwtUserDetails) {
        Long userId = jwtUserDetails.userId();
        return ResponseEntity.ok(orderService.findByUserId(userId, pageable));
    }

    @Operation(summary = "특정 주문 조회", description = "주문 ID로 특정 주문을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReadUserOrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/{orderId}/users")
    public ResponseEntity<ReadUserOrderResponse> findByOrderIdAndUserId(@PathVariable String orderId, @CurrentUser JwtUserDetails jwtUserDetails) {
        Long userId = jwtUserDetails.userId();
        return ResponseEntity.ok(orderService.findByOrderIdAndUserId(orderId, userId));
    }

    @Operation(summary = "주문 상태 조회", description = "주문 ID로 주문 상태를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReadOrderStatusResponse.class))),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/status/{orderId}")
    public ResponseEntity<ReadOrderStatusResponse> find(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.findOrderStatusByOrderId(orderId));
    }

    @Operation(summary = "주문 업데이트", description = "주문 ID로 주문 상태를 업데이트합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업데이트 성공", content = @Content(schema = @Schema(implementation = UpdateOrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PutMapping("/{orderId}")
    public ResponseEntity<UpdateOrderResponse> update(@PathVariable String orderId, @RequestBody UpdateOrderRequest request, @CurrentUser JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok(orderService.updateOrderStatusByOrderId(orderId, request, jwtUserDetails.userId()));
    }

    @Operation(summary = "주문 배송 정보 조회", description = "주문 ID로 배송 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReadOrderDeliveryResponse.class))),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/{orderId}/delivery")
    public ResponseEntity<ReadOrderDeliveryResponse> getDelivery(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getByOrderIdAndUserId(orderId));
    }

    @Operation(summary = "주문 상세 정보 조회", description = "주문 ID로 주문 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReadOrderDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<ReadOrderDetailResponse> getOrder(@PathVariable String orderId, @CurrentUser JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok(orderService.getOrderByOrderId(orderId, jwtUserDetails.userId()));
    }

    @Operation(summary = "주문 순수 금액 로그 조회", description = "모든 회원에 대해 주어진 날짜의 주문 순수 금액을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/logs")
    public ResponseEntity<List<ReadPurePriceResponse>> getPurePrices(@RequestParam LocalDate date) {
        return ResponseEntity.ok(orderService.getPurePriceByDate(date));
    }

    @Operation(summary = "비회원 주문 조회", description = "비회원의 주문 ID와 이메일로 주문 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReadOrderDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/none/{orderId}")
    public ResponseEntity<ReadOrderDetailResponse> getOrderNoneMember(@PathVariable String orderId, @RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrderByOrderIdAndEmail(orderId, email));
    }

    @Operation(summary = "모든 주문 상태 업데이트", description = "모든 주문 상태를 완료로 업데이트합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "업데이트 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PutMapping("/delivery/done")
    public ResponseEntity<Void> updateAllOrderStatusInDone() {
        orderService.updateOrderStatusToDone();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "주문 내역 존재 여부 확인", description = "사용자의 주문 내역 존재 여부를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/exist")
    public ResponseEntity<Boolean> getOrderHistory(@CurrentUser JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok(orderService.existOrderHistoryByUserId(jwtUserDetails.userId()));
    }
}
