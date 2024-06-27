package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointRequest;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
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
public class PointConsumer {

    private final UserAdaptor userAdaptor;
    private final MessageProducer messageProducer;

    @RabbitListener(queues = "pointUsedQueue")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000))
    public void updatePoints(UpdatePointMessage message) {
        log.info("주문이 확정되어 포인트가 적립됩니다. : {}", message.usePoints());
        UpdatePointRequest updatePointRequest = UpdatePointRequest.from(message);

        userAdaptor.updatePoint(updatePointRequest);
    }

    @Recover
    public void pointRecover(Throwable throwable, UpdatePointMessage message) {
        log.error("포인트 적립에 실패하였습니다. 예외 : {}", throwable.getMessage());

        messageProducer.sendDlxMessage("dlxExchange", "dlx.orderDoneQueue", message);
    }
}
