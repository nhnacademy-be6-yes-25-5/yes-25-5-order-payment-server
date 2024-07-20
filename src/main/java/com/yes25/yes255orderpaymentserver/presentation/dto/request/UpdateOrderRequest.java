package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;

public record UpdateOrderRequest(OrderStatusType orderStatusType, PaymentProvider paymentProvider) {

}
