package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateUserCartQuantityRequest;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartConsumer {

    private final UserAdaptor userAdaptor;
    private final MessageProducer messageProducer;

    @RabbitListener(queues = "cartDecreaseQueue")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000))
    public void receiveCartDecreaseQueue(List<UpdateUserCartQuantityRequest> requests) {
        log.info("주문이 확정되어 장바구니 재고를 감소시킵니다. 요청 수 : {}", requests.size());
        requests.forEach(request ->
            log.info("책 ID : {}, 수량 : {}", request.bookId(), request.quantity()));

        userAdaptor.decreaseUserCartQuantity(requests);
    }

    @Recover
    public void cartDecreaseRecover(Throwable throwable, List<UpdateUserCartQuantityRequest> requests) {
        log.error("장바구니 재고 감소에 실패하였습니다. 예외 : {}", throwable.getMessage());

        messageProducer.sendDlxMessage("dlxExchange", "dlx.cartDecreaseQueue", requests);
    }
}