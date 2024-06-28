package com.yes25.yes255orderpaymentserver.application.service.queue.producer;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateUserCartQuantityRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreateOrderResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
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

    public void sendOrderDone(PreOrder preOrder, BigDecimal purePrice, String authToken) {
        try {
            MessagePostProcessor messagePostProcessor = message -> {
                message.getMessageProperties().setHeader("Authorization", authToken);
                return message;
            };

            StockRequest stockRequest = StockRequest.of(preOrder.getBookIds(),
                preOrder.getQuantities(), OperationType.INCREASE);
            UpdatePointMessage updatePointMessage = UpdatePointMessage.of(preOrder, purePrice);
            List<UpdateUserCartQuantityRequest> userCartQuantityRequests = createUserCartQuantityRequests(
                preOrder);

            rabbitTemplate.convertAndSend("stockDecreaseExchange", "stockDecreaseRoutingKey",
                stockRequest, messagePostProcessor);

            rabbitTemplate.convertAndSend("pointUsedExchange", "pointUsedRoutingKey",
                updatePointMessage, messagePostProcessor);

            rabbitTemplate.convertAndSend("couponUsedExchange", "couponUsedRoutingKey",
                preOrder.getCouponId(), messagePostProcessor);

            rabbitTemplate.convertAndSend("cartDecreaseExchange", "cartDecreaseRoutingKey",
                userCartQuantityRequests, messagePostProcessor);

            log.info("상품 재고 감소, 장바구니 재고 감소, 쿠폰 사용, 포인트 차감 및 적립 메세지가 발행되었습니다.");
        } catch (Exception e) {
            log.error("error :", e);
        }
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
