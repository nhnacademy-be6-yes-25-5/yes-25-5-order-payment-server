package com.yes25.yes255orderpaymentserver.application.service.event;

import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.util.List;
import lombok.Builder;

@Builder
public record OrderCreateEvent(List<Long> bookIdList, List<Integer> quantityList, String paymentKey, String orderId, Integer cancelAmount) {


    public static OrderCreateEvent of(PreOrder preOrder, SuccessPaymentResponse response) {
        return OrderCreateEvent.builder()
            .bookIdList(preOrder.getBookIds())
            .quantityList(preOrder.getQuantities())
            .orderId(preOrder.getPreOrderId())
            .paymentKey(response.paymentKey())
            .cancelAmount(response.paymentAmount())
            .build();
    }
}
