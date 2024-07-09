package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;

public interface PreOrderService {

    CreateOrderResponse savePreOrder(CreateOrderRequest request, Long userId);

    PreOrder getPreOrder(String orderId);
}
