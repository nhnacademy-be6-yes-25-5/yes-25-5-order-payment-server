package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;

public record UpdateOrderRequest(OrderStatusType orderStatusType) {

}
