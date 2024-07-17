package com.yes25.yes255orderpaymentserver.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes25.yes255orderpaymentserver.application.dto.response.ReadPurePriceResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.PreOrderService;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDeliveryResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDetailResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private PreOrderService preOrderService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
        objectMapper = new ObjectMapper();
    }

    @DisplayName("주문을 생성하는지 확인한다.")
    @Test
    void create() throws Exception {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder().build();
        CreateOrderResponse response = CreateOrderResponse.builder().build();
        when(preOrderService.savePreOrder(any(CreateOrderRequest.class), any())).thenReturn(response);

        // when && then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @DisplayName("사용자의 모든 주문 내역을 조회하는지 확인한다.")
    @Test
    void findAllOrderByUserId() throws Exception {
        // given
        JwtUserDetails jwtUserDetails = new JwtUserDetails(1L, List.of(), "token", "refreshToken");
        Page<ReadUserOrderAllResponse> responsePage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        // when && then
        mockMvc.perform(get("/orders/users")
                .with(csrf())
                .requestAttr("jwtUserDetails", jwtUserDetails))
            .andExpect(status().isOk());
    }

    @DisplayName("특정 주문을 조회하는지 확인한다.")
    @Test
    void findByOrderIdAndUserId() throws Exception {
        // given
        JwtUserDetails jwtUserDetails = new JwtUserDetails(1L, List.of(), "token", "refreshToken");
        ReadUserOrderResponse response = ReadUserOrderResponse.builder().build();

        // when && then
        mockMvc.perform(get("/orders/{orderId}/users", "order1")
                .with(csrf())
                .requestAttr("jwtUserDetails", jwtUserDetails))
            .andExpect(status().isOk());
    }

    @DisplayName("주문 상태를 조회하는지 확인한다.")
    @Test
    void find() throws Exception {
        // given
        JwtUserDetails jwtUserDetails = new JwtUserDetails(1L, List.of(), "token", "refreshToken");
        ReadOrderStatusResponse response = ReadOrderStatusResponse.builder().build();
        when(orderService.findOrderStatusByOrderId(any(String.class))).thenReturn(response);

        // when && then
        mockMvc.perform(get("/orders/status/{orderId}", "order1")
                .with(csrf())
                .requestAttr("jwtUserDetails", jwtUserDetails))
            .andExpect(status().isOk());
    }

    @DisplayName("주문을 업데이트하는지 확인한다.")
    @Test
    void update() throws Exception {
        // given
        String orderId = "orderId";
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.DONE);
        JwtUserDetails jwtUserDetails = JwtUserDetails.builder()
            .userId(1L)
            .roles(List.of(new SimpleGrantedAuthority("ROLE_USER")))
            .accessToken("token")
            .refreshToken("refreshToken")
            .build();

        // when & then
        mockMvc.perform(put("/orders/{orderId}", orderId)
                .with(SecurityMockMvcRequestPostProcessors.user(jwtUserDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @DisplayName("주문 배송 정보를 조회하는지 확인한다.")
    @Test
    void getDelivery() throws Exception {
        // given
        ReadOrderDeliveryResponse response = ReadOrderDeliveryResponse.builder().build();
        when(orderService.getByOrderIdAndUserId(any(String.class))).thenReturn(response);

        // when && then
        mockMvc.perform(get("/orders/{orderId}/delivery", "order1")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @DisplayName("주문 상세 정보를 조회하는지 확인한다.")
    @Test
    void getOrder() throws Exception {
        // given
        JwtUserDetails jwtUserDetails = new JwtUserDetails(1L, List.of(), "token", "refreshToken");
        ReadOrderDetailResponse response = ReadOrderDetailResponse.builder().build();

        // when && then
        mockMvc.perform(get("/orders/{orderId}", "order1")
                .with(csrf())
                .requestAttr("jwtUserDetails", jwtUserDetails))
            .andExpect(status().isOk());
    }

    @DisplayName("주문 순수 금액 로그를 조회하는지 확인한다.")
    @Test
    void getPurePrices() throws Exception {
        // given
        JwtUserDetails jwtUserDetails = new JwtUserDetails(1L, List.of(), "token", "refreshToken");
        List<ReadPurePriceResponse> responses = Collections.emptyList();
        when(orderService.getPurePriceByDate(any(LocalDate.class))).thenReturn(responses);

        // when && then
        mockMvc.perform(get("/orders/logs")
                .param("date", "2024-07-16")
                .with(csrf())
                .requestAttr("jwtUserDetails", jwtUserDetails))
            .andExpect(status().isOk());
    }

    @DisplayName("비회원의 주문 정보를 조회하는지 확인한다.")
    @Test
    void getOrderNoneMember() throws Exception {
        // given
        ReadOrderDetailResponse response = ReadOrderDetailResponse.builder().build();
        when(orderService.getOrderByOrderIdAndEmail(any(String.class), any(String.class))).thenReturn(response);

        // when && then
        mockMvc.perform(get("/orders/none/{orderId}", "order1")
                .param("email", "test@example.com")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @DisplayName("모든 주문 상태를 완료로 업데이트하는지 확인한다.")
    @Test
    void updateAllOrderStatusInDone() throws Exception {
        // given
        JwtUserDetails jwtUserDetails = new JwtUserDetails(1L, List.of(), "token", "refreshToken");

        // when && then
        mockMvc.perform(put("/orders/delivery/done")
                .with(csrf())
                .requestAttr("jwtUserDetails", jwtUserDetails))
            .andExpect(status().isNoContent());
    }

    @DisplayName("사용자의 주문 내역 존재 여부를 확인하는지 확인한다.")
    @Test
    void getOrderHistory() throws Exception {
        // given
        JwtUserDetails jwtUserDetails = new JwtUserDetails(1L, List.of(), "token", "refreshToken");

        // when && then
        mockMvc.perform(get("/orders/exist")
                .principal(() -> String.valueOf(jwtUserDetails.userId()))
                .param("bookId", "1")
                .with(csrf()))
            .andExpect(status().isOk());
    }
}
