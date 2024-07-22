package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateOrderResponse(String orderId,
                                  Integer totalAmount,
                                  List<Long> bookIds,
                                  List<Integer> quantities,
                                  Integer points,
                                  String paymentProvider) {

    public static CreateOrderResponse fromRequest(PreOrder preOrder) {
        return CreateOrderResponse.builder()
            .orderId(preOrder.getPreOrderId())
            .totalAmount(preOrder.getOrderTotalAmount().intValue())
            .bookIds(preOrder.getBookIds())
            .quantities(preOrder.getQuantities())
            .points(preOrder.getPoints().intValue())
            .paymentProvider(preOrder.getPaymentProvider().name().toLowerCase())
            .build();
    }
}
