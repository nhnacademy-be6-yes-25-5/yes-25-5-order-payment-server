package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.PreOrderService;
import com.yes25.yes255orderpaymentserver.application.service.context.PaymentContext;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.exception.PaymentException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService orderService;
    private final PreOrderService preOrderService;
    private final MessageProducer messageProducer;
    private final PaymentContext paymentContext;

    /**
     * @throws PaymentException 결제 완료 후, 결제의 preOrderId와 주문의 orderId가 일치하지 않으면 발생합니다.
     */
    @RabbitListener(queues = "payQueue")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
    public void receivePayment(SuccessPaymentResponse response, Message message) {
        MessageProperties properties = message.getMessageProperties();
        String authToken = (String) properties.getHeaders().get("Authorization");

        if (Objects.isNull(authToken)) {
            log.error("인증 헤더가 비어있습니다. 비회원으로 처리합니다.");
        }

        PreOrder preOrder = preOrderService.getPreOrder(response.orderId());

        if (Objects.isNull(preOrder)) {
            log.error("주문 정보를 가져오지 못했습니다.");
            throw new PaymentException(
                ErrorStatus.toErrorStatus("결제 큐에서 주문을 가져오지 못했습니다.", 404,
                    LocalDateTime.now()), response.paymentKey(), response.orderId(), response.paymentAmount());
        }

        BigDecimal purePrice = preOrder.calculatePurePrice();
        orderService.createOrder(preOrder, purePrice, response);
        messageProducer.sendOrderDone(preOrder, purePrice, authToken, preOrder.getCartId());
    }

    @Recover
    public void recover(Exception e, SuccessPaymentResponse response, Message message) {
        log.error("최대 재시도 횟수 초과. 재고 증가와 결제 취소 요청을 보냅니다. 주문 ID : {}", response.orderId());
        StockRequest stockRequest = StockRequest.of(response.bookIdList(), response.quantityList(), OperationType.INCREASE);
        MessageProperties properties = message.getMessageProperties();
        String authToken = (String) properties.getHeaders().get("Authorization");

        messageProducer.sendMessage("stockDecreaseExchange", "stockDecreaseRoutingKey", stockRequest, authToken);
        paymentContext.cancelPayment(response.paymentKey(), "결제 처리 중 예상치 못한 예외 발생", response.paymentAmount(), response.orderId(), response.paymentProvider().name().toLowerCase());
    }
}
