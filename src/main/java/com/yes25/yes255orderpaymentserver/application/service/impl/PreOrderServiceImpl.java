package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.service.PreOrderService;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreOrderServiceImpl implements PreOrderService {
    private static final String PRE_ORDER_MAP = "preOrderMap";
    private static final long TTL = 20;

    private final RedissonClient redissonClient;

    @Override
    public CreateOrderResponse savePreOrder(CreateOrderRequest request, Long userId) {
        RMapCache<String, PreOrder> map = redissonClient.getMapCache(PRE_ORDER_MAP);
        PreOrder preOrder = PreOrder.from(request, userId);
        map.put(preOrder.getPreOrderId(), preOrder, TTL, TimeUnit.MINUTES);

        return CreateOrderResponse.fromRequest(preOrder);
    }

    @Override
    public PreOrder getPreOrder(String orderId) {
        RMapCache<String, PreOrder> mapCache = redissonClient.getMapCache(PRE_ORDER_MAP);

        return mapCache.get(orderId);
    }
}
