package com.yes25.yes255orderpaymentserver.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PreOrderServiceImplTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RMapCache<String, PreOrder> mapCache;

    @InjectMocks
    private PreOrderServiceImpl preOrderService;

    @BeforeEach
    void setUp() {
        when(redissonClient.<String, PreOrder>getMapCache(anyString())).thenReturn(mapCache);
    }

    @Test
    @DisplayName("PreOrder 저장 테스트")
    void savePreOrder() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .orderId("order")
            .orderTotalAmount(BigDecimal.valueOf(1000))
            .productIds(List.of(1L))
            .quantities(List.of(1))
            .points(BigDecimal.valueOf(1000))
            .build();
        Long userId = 1L;
        PreOrder preOrder = PreOrder.from(request, userId);

        // when
        when(mapCache.put(anyString(), any(PreOrder.class), any(Long.class), any(TimeUnit.class))).thenReturn(null);
        CreateOrderResponse response = preOrderService.savePreOrder(request, userId);

        // then
        assertEquals(response.orderId(), preOrder.getPreOrderId());
    }

    @Test
    @DisplayName("PreOrder 조회 테스트")
    void getPreOrder() {
        // given
        String orderId = "testOrderId";
        PreOrder preOrder = PreOrder.builder().build();

        // when
        when(mapCache.get(orderId)).thenReturn(preOrder);
        PreOrder result = preOrderService.getPreOrder(orderId);

        // then
        verify(mapCache, times(1)).get(orderId);
        assertEquals(result, preOrder);
    }
}
