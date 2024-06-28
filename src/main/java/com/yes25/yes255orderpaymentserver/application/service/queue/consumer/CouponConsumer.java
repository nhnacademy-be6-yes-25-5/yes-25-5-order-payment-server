package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.utils.AsyncSecurityContextUtils;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.CouponAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponConsumer {

    private final CouponAdaptor couponAdaptor;
    private final AsyncSecurityContextUtils securityContextUtils;
    private final MessageProducer messageProducer;

    @RabbitListener(queues = "couponUsedQueue")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000))
    public void receivePointUpdateDone(Long couponId, Message message) {
        log.info("주문이 확정되어 쿠폰을 사용 처리 합니다. 쿠폰 ID : {}", couponId);
        securityContextUtils.configureSecurityContext(message);
        couponAdaptor.updateCouponStatus(couponId);
    }

    @Recover
    public void couponRecover(Throwable throwable, Long couponId) {
        log.error("쿠폰 사용 처리에 실패하였습니다. 쿠폰 ID : {}, 예외 : {}", couponId, throwable.getMessage());

        messageProducer.sendDlxMessage("dlxExchange", "dlx.couponUsedQueue", couponId);
    }
}
