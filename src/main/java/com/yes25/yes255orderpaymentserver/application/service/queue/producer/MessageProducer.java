package com.yes25.yes255orderpaymentserver.application.service.queue.producer;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateUserCartQuantityRequest;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {

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
        List<UpdateUserCartQuantityRequest> userCartQuantityRequests = createUserCartQuantityRequests(preOrder);

        rabbitTemplate.convertAndSend("pointUsedExchange", "pointUsedRoutingKey",
            updatePointMessage);

        rabbitTemplate.convertAndSend("couponUsedExchange", "couponUsedRoutingKey",
            preOrder.getCouponId());

        rabbitTemplate.convertAndSend("cartDecreaseExchange", "cartDecreaseRoutingKey",
            userCartQuantityRequests);
    }

    private List<UpdateUserCartQuantityRequest> createUserCartQuantityRequests(PreOrder preOrder) {

        List<UpdateUserCartQuantityRequest> requests = new ArrayList<>();
        for (int i = 0; i < preOrder.getBookIds().size(); i++) {
            UpdateUserCartQuantityRequest request = UpdateUserCartQuantityRequest.of(preOrder.getBookIds().get(i), preOrder.getQuantities().get(i));
            requests.add(request);
        }

        return requests;
    }

    public <T> void sendDlxMessage(String dlxExchange, String routingKey, T message) {
        rabbitTemplate.convertAndSend(dlxExchange, routingKey, message);
    }
}
