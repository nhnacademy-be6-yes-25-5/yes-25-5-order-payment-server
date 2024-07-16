package com.yes25.yes255orderpaymentserver.presentation.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes25.yes255orderpaymentserver.application.service.AdminOrderService;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtFilter;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtProvider;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CancelOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderStatusRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CancelOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllUserOrderCancelStatusResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminOrderController.class)
@WithMockUser
class AdminOrderControllerTest {

    @MockBean
    private AdminOrderService adminOrderService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        when(jwtProvider.getUserNameFromToken(anyString())).thenReturn("user");
    }

    @DisplayName("관리자가 회원 혹은 비회원에 대한 주문 내역을 조회하는지 확인한다.")
    @ParameterizedTest
    @ValueSource(strings = {"MEMBER", "NONE_MEMBER"})
    void getAllOrders(String role) throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReadAllOrderResponse> responses = new PageImpl<>(List.of(), pageable, 0);
        when(adminOrderService.getAllOrdersByPaging(pageable, role)).thenReturn(responses);

        // when && then
        mockMvc.perform(get("/orders/admin")
                .param("role", role))
            .andExpect(status().isOk());
    }

    @DisplayName("관리자가 주문 ID로 상태를 업데이트하는지 확인한다.")
    @Test
    void update() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatusType.CANCEL);
        String requestBody = objectMapper.writeValueAsString(request);

        String orderId = "order";
        doNothing().when(adminOrderService).updateOrderStatusByOrderId(orderId, request);


        // when && then
        mockMvc.perform(put("/orders/admin/" + orderId)
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isOk());
    }

    @DisplayName("관리자가 환불 주문 내역을 조회하는지 확인한다.")
    @Test
    void testGetAllOrders() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReadAllUserOrderCancelStatusResponse> responses = new PageImpl<>(List.of(), pageable, 0);
        when(adminOrderService.getAllCancelOrdersByPaging(pageable)).thenReturn(responses);

        // when && then
        mockMvc.perform(get("/orders/admin/refund"))
            .andExpect(status().isOk());

    }

    @DisplayName("관리자가 주문 ID를 통해 환불을 처리하는지 확인한다.")
    @Test
    void cancelOrder() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        CancelOrderRequest request = new CancelOrderRequest(CancelStatus.ACCESS);
        String orderId = "order";
        CancelOrderResponse response = CancelOrderResponse.from(CancelStatus.ACCESS);

        when(adminOrderService.cancelOrderByOrderId(orderId, request)).thenReturn(response);

        String requestBody = objectMapper.writeValueAsString(request);

        // when && then
        mockMvc.perform(put("/orders/admin/" + orderId + "/refund")
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isOk());
    }
}