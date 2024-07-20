package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;

public interface OrderStatusService {

    UpdateOrderResponse updateOrderStatusByOrderId(String orderId, UpdateOrderRequest request, Long userId);
}
