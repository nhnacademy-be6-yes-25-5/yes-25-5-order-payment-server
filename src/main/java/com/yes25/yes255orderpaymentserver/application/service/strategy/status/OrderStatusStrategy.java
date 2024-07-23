package com.yes25.yes255orderpaymentserver.application.service.strategy.status;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;

public interface OrderStatusStrategy {

    void updateOrderStatus(Order order, UpdateOrderRequest request);
}
