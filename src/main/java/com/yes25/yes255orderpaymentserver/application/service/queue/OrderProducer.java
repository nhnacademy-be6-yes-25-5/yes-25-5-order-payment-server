package com.yes25.yes255orderpaymentserver.application.service.queue;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public CreateOrderResponse sendCreateOrder(CreateOrderRequest request) {
        PreOrder preOrder = PreOrder.from(request);
        sendPreOrder(preOrder);

        log.info("가주문이 발행되었습니다. : {}", preOrder);

        return CreateOrderResponse.fromRequest(preOrder);
    }

    public void sendCancelMessage(String orderId) {
        rabbitTemplate.convertAndSend("cancelExchange", "cancelRoutingKey", orderId);
    }

    public void sendPreOrder(PreOrder preOrder) {
        rabbitTemplate.convertAndSend("preOrderExchange", "preOrderRoutingKey", preOrder);
    }

    public void sendOrderDone(PreOrder preOrder, BigDecimal purePrice) {
        UpdatePointMessage updatePointMessage = UpdatePointMessage.from(preOrder, purePrice);

        rabbitTemplate.convertAndSend("orderDoneExchange", "orderDoneRoutingKey",
            updatePointMessage);
    }
}
