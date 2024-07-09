package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.service.PreOrderService;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreOrderServiceImpl implements PreOrderService {

    private final RedissonClient redissonClient;
    private static final String PRE_ORDER_MAP = "preOrderMap";

    @Override
    public CreateOrderResponse savePreOrder(CreateOrderRequest request) {
        RMap<String, PreOrder> map = redissonClient.getMap(PRE_ORDER_MAP);
        PreOrder preOrder = PreOrder.from(request);
        map.put(preOrder.getPreOrderId(), preOrder);

        return CreateOrderResponse.fromRequest(preOrder);
    }

    @Override
    public PreOrder getPreOrder(String orderId) {
        RMap<String, PreOrder> map = redissonClient.getMap(PRE_ORDER_MAP);

        return map.get(orderId);
    }
}
