package com.yes25.yes255orderpaymentserver.application.service.queue;

import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.common.exception.ApplicationException;
import com.yes25.yes255orderpaymentserver.common.exception.PaymentException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
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
    private final BookAdaptor bookAdaptor;
    private final OrderService orderService;

    @RabbitListener(queues = "paymentQueue")
    public void receivePayment(String orderId) {
        PreOrder preOrder = (PreOrder) rabbitTemplate.receiveAndConvert("preOrderQueue");

        if (Objects.isNull(preOrder) || !preOrder.getOrderId().equals(orderId)) {
            throw new PaymentException(
                ErrorStatus.toErrorStatus("결제 큐에서 해당하는 주문를 찾을 수 없습니다.", 404,
                    LocalDateTime.now())
            );
        }

        log.info("결제가 완료되어 가주문을 큐에서 꺼냈습니다. : {}", preOrder.toString());

        for (int i = 0; i < preOrder.getBookIds().size(); i++) {
//            boolean inStock = bookAdaptor.checkStock(preOrder.getBookIds().get(i));
            boolean inStock = true;
            if (!inStock) {
                log.error(
                    "PreOrder is null or OrderId does not match. PreOrder: {}, OrderId: {}",
                    preOrder, orderId);
                throw new ApplicationException(
                    ErrorStatus.toErrorStatus("재고가 부족한 책이 있습니다. : " + preOrder.getBookIds().get(i), 400,
                        LocalDateTime.now())
                );
            }
        }

        orderService.save(preOrder);
    }
}
