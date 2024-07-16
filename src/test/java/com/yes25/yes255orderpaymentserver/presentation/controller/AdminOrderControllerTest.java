package com.yes25.yes255orderpaymentserver.presentation.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yes25.yes255orderpaymentserver.application.service.AdminOrderService;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtFilter;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtProvider;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        Page<ReadAllOrderResponse> responses = new PageImpl<>(List.of(), pageable, 1);
        when(adminOrderService.getAllOrdersByPaging(pageable, role)).thenReturn(responses);

        // when && then
        mockMvc.perform(get("/orders/admin")
                .param("role", role))
            .andExpect(status().isOk());
    }

    @Test
    void update() {
    }

    @Test
    void testGetAllOrders() {
    }

    @Test
    void cancelOrder() {
    }
}