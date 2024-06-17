package com.yes25.yes255orderpaymentserver.application.service.queue;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
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

        rabbitTemplate.convertAndSend("preOrderExchange", "preOrderRoutingKey", preOrder);
        log.info("가주문이 발행되었습니다. : {}", preOrder);

        return CreateOrderResponse.fromRequest(preOrder.getOrderId());
    }

}
