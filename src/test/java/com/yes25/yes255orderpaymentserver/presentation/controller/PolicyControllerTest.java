package com.yes25.yes255orderpaymentserver.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes25.yes255orderpaymentserver.application.service.PolicyService;
import com.yes25.yes255orderpaymentserver.application.service.impl.TestUserDetailsService;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.TakeoutType;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadShippingPolicyResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadTakeoutResponse;
import java.util.List;
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
class PolicyControllerTest {

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private PolicyController policyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new TestUserDetailsService();

        mockMvc = MockMvcBuilders.standaloneSetup(policyController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("모든 배송 정책 조회 성공")
    void findAllShippingPoliciesSuccess() throws Exception {
        // given && when && then
        mockMvc.perform(get("/policies/shipping"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("무료 배송 정책 조회 성공")
    void findFreeShippingPolicySuccess() throws Exception {
        // given
        ReadShippingPolicyResponse freeShippingPolicy = new ReadShippingPolicyResponse(1L, 5000, 30000);

        when(policyService.findFreeShippingPolicy()).thenReturn(freeShippingPolicy);

        // when && then
        mockMvc.perform(get("/policies/shipping/free")
                .contentType("application/json"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모든 픽업 정책 조회 성공")
    void findAllTakeoutPoliciesSuccess() throws Exception {
        // given
        List<ReadTakeoutResponse> takeoutPolicies = List.of(
            new ReadTakeoutResponse(TakeoutType.PAPER, 2000, "종이"));

        when(policyService.findAllTakeoutPolicy()).thenReturn(takeoutPolicies);

        // when && then
        mockMvc.perform(get("/policies/takeout")
                .contentType("application/json"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모든 배송 정책 조회 시 JwtUserDetails가 있을 때 성공")
    void findAllShippingPoliciesWithJwtUserDetailsSuccess() throws Exception {
        // given && when && then
        mockMvc.perform(get("/policies/shipping")
                .contentType("application/json")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetailsService.loadUserByUsername("username"))))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("무료 배송 정책 조회 시 JwtUserDetails가 있을 때 성공")
    void findFreeShippingPolicyWithJwtUserDetailsSuccess() throws Exception {
        // given
        ReadShippingPolicyResponse freeShippingPolicy = new ReadShippingPolicyResponse(1L, 5000, 30000);

        when(policyService.findFreeShippingPolicy()).thenReturn(freeShippingPolicy);

        // when && then
        mockMvc.perform(get("/policies/shipping/free")
                .contentType("application/json")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetailsService.loadUserByUsername("username"))))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모든 픽업 정책 조회 시 JwtUserDetails가 있을 때 성공")
    void findAllTakeoutPoliciesWithJwtUserDetailsSuccess() throws Exception {
        List<ReadTakeoutResponse> takeoutPolicies = List.of(new ReadTakeoutResponse(TakeoutType.PAPER, 2000, "종이"));

        when(policyService.findAllTakeoutPolicy()).thenReturn(takeoutPolicies);

        mockMvc.perform(get("/policies/takeout")
                .contentType("application/json")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetailsService.loadUserByUsername("username"))))
            .andExpect(status().isOk());
    }
}
