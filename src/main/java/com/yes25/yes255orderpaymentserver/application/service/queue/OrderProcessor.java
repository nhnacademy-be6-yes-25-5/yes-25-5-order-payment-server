package com.yes25.yes255orderpaymentserver.application.service.queue;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointRequest;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.common.exception.PaymentException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
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
    private final BookAdaptor bookAdaptor;
    private final UserAdaptor userAdaptor;

    /**
     * @throws PaymentException 결제 완료 후, 결제의 preOrderId와 주문의 orderId가 일치하지 않으면 발생합니다.
     * */
    @RabbitListener(queues = "paymentQueue")
    public void receivePayment(SuccessPaymentResponse response) {
        PreOrder preOrder = (PreOrder) rabbitTemplate.receiveAndConvert("preOrderQueue");

        if (Objects.isNull(preOrder) || !preOrder.getPreOrderId().equals(response.orderId())) {
            throw new PaymentException(
                ErrorStatus.toErrorStatus("결제 큐에서 해당하는 주문를 찾을 수 없습니다.", 404,
                    LocalDateTime.now()), response.paymentKey());
        }

        log.info("결제가 완료되어 가주문을 큐에서 꺼냈습니다. : {}", preOrder);

        for (int i = 0; i < preOrder.getBookIds().size(); i++) {
            bookAdaptor.decreaseStock(preOrder.getBookIds().get(i),
                preOrder.getQuantities().get(i));
        }

        orderService.save(preOrder);
        updatePoints(preOrder);
    }

    private void updatePoints(PreOrder preOrder) {
        UpdatePointRequest updatePointRequest = UpdatePointRequest.from(preOrder);
        userAdaptor.updatePoint(preOrder.getUserId(), updatePointRequest);
    }
}
