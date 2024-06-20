package com.yes25.yes255orderpaymentserver.application.service.queue;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointRequest;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.common.exception.PaymentException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;
    private final UserAdaptor userAdaptor;
    private final OrderProducer orderProducer;

    /**
     * @throws PaymentException 결제 완료 후, 결제의 preOrderId와 주문의 orderId가 일치하지 않으면 발생합니다. 재고 확인 및 포인트
     *                          적립은 타 서버 완료 시 확인이 가능합니다. 현재는 주석처리 하였습니다.
     */
    @RabbitListener(queues = "paymentQueue")
    public void receivePayment(SuccessPaymentResponse response) {
        PreOrder preOrder = (PreOrder) rabbitTemplate.receiveAndConvert("preOrderQueue");

        if (Objects.isNull(preOrder)) {
            log.error("주문 정보를 가져오지 못했습니다.");
            throw new PaymentException(
                ErrorStatus.toErrorStatus("결제 큐에서 주문을 가져오지 못했습니다.", 404, LocalDateTime.now()), response.paymentKey());
        }

        if (!preOrder.getPreOrderId().equals(response.orderId())) {
            log.error("주문 정보가 일치하지 않습니다.");
            orderProducer.sendPreOrder(preOrder);

            throw new PaymentException(
                ErrorStatus.toErrorStatus("결제 큐에서 해당하는 주문를 찾을 수 없습니다.", 404,
                    LocalDateTime.now()), response.paymentKey());
        }

        BigDecimal purePrice = preOrder.calculatePurePrice();
        orderService.createOrder(preOrder, purePrice);
        orderProducer.sendOrderDone(preOrder, purePrice);
    }


    /**
     * @param updatePointMessage 주문 확정을 알리는 메세지
     * */
    @RabbitListener(queues = "orderDoneQueue")
    private void updatePoints(UpdatePointMessage updatePointMessage) {
        UpdatePointRequest updatePointRequest = UpdatePointRequest.from(updatePointMessage);
        userAdaptor.updatePoint(updatePointMessage.userId(), updatePointRequest);
    }

    /**
     * @param orderId 가주문 Id
     * */
    @RabbitListener(queues = "cancelQueue")
    public void receiveCancelMessage(String orderId) {

        boolean orderCancelled = false;

        while (!orderCancelled) {
            PreOrder preOrder = (PreOrder) rabbitTemplate.receiveAndConvert("preOrderQueue");

            if (Objects.isNull(preOrder)) {
                log.info("주문 정보를 가져오지 못했습니다. 큐에 더이상 메세지가 존재하지 않습니다.");

                orderCancelled = true;
            } else if (preOrder.getPreOrderId().equals(orderId)) {
                log.info("재고가 부족하여 주문을 취소합니다. 주문 ID : {}", orderId);
                orderCancelled = true;
            } else {
                rabbitTemplate.convertAndSend("preOrderExchange", "preOrderRoutingKey", preOrder);
            }
        }
    }
}
