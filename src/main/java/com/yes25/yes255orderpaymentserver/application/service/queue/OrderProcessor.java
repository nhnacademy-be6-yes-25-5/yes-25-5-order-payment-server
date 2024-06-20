package com.yes25.yes255orderpaymentserver.application.service.queue;

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
public class OrderProcessor {

    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;
    private final UserAdaptor userAdaptor;

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
                rabbitTemplate.convertAndSend("preOrderExchange", "preOrderRoutingKey", preOrder);

            throw new PaymentException(
                ErrorStatus.toErrorStatus("결제 큐에서 해당하는 주문를 찾을 수 없습니다.", 404,
                    LocalDateTime.now()), response.paymentKey());
        }

        BigDecimal purePrice = preOrder.calculatePurePrice();

        orderService.createOrder(preOrder, purePrice);
//        updatePoints(preOrder, purePrice);
    }

    private void updatePoints(PreOrder preOrder, BigDecimal purePrice) {
        UpdatePointRequest updatePointRequest = UpdatePointRequest.from(preOrder, purePrice);
        userAdaptor.updatePoint(preOrder.getUserId(), updatePointRequest);
    }
}
