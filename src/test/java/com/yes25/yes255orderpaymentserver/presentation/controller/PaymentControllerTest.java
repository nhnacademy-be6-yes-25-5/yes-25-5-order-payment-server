package com.yes25.yes255orderpaymentserver.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.context.PaymentContext;
import com.yes25.yes255orderpaymentserver.application.service.impl.TestUserDetailsService;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentContext paymentContext;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new TestUserDetailsService();

        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
        objectMapper = new ObjectMapper();
    }

    @DisplayName("회원의 결제를 승인하는지 확인한다.")
    @Test
    void confirmPaymentForMember() throws Exception {
        // given
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder().build();
        String requestBody = objectMapper.writeValueAsString(createOrderRequest);

        // when && then
        mockMvc.perform(post("/payments/confirm")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetailsService.loadUserByUsername("username")))
                .content(requestBody)
                .contentType("application/json"))
            .andExpect(status().isOk());
    }

    @DisplayName("회원의 주문별 결제 내역 조회를 하는지 확인한다.")
    @Test
    void findAllForMember() throws Exception {
        // given
        String orderId = "order";

        // when && then
        mockMvc.perform(get("/payments/{orderId}", orderId)
                .with(SecurityMockMvcRequestPostProcessors.user(userDetailsService.loadUserByUsername("username"))))
            .andExpect(status().isOk());
    }

    @DisplayName("회원의 0원 결제를 승인하는지 확인한다.")
    @Test
    void confirmPaymentByZeroForMember() throws Exception {
        // given
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder().build();
        String requestBody = objectMapper.writeValueAsString(createOrderRequest);

        // when && then
        mockMvc.perform(post("/payments/confirm/zero")
                .content(requestBody)
                .contentType("application/json")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetailsService.loadUserByUsername("username"))))
            .andExpect(status().isOk());
    }
}