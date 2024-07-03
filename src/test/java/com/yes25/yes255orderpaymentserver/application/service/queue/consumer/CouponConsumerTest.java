package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateCouponRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.utils.AsyncSecurityContextUtils;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.CouponAdaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

@ExtendWith(MockitoExtension.class)
class CouponConsumerTest {

    @Mock
    private CouponAdaptor couponAdaptor;

    @Mock
    private AsyncSecurityContextUtils securityContextUtils;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private CouponConsumer couponConsumer;

    private UpdateCouponRequest useRequest;
    private UpdateCouponRequest rollbackRequest;
    private Message message;

    @BeforeEach
    void setUp() {
        useRequest = UpdateCouponRequest.from(1L, OperationType.USE);
        rollbackRequest = UpdateCouponRequest.from(1L, OperationType.ROLLBACK);

        message = new Message(new byte[0]);
    }

    @DisplayName("쿠폰을 사용 요청을 보내는지 확인한다.")
    @Test
    void receiveCouponUpdateRequestInUse() {
        // given
        doNothing().when(securityContextUtils).configureSecurityContext(any(Message.class));

        // when
        couponConsumer.receiveCouponUpdateRequestInUse(useRequest, message);

        // then
        verify(couponAdaptor, times(1)).updateCouponStatus(useRequest.couponId(),
            useRequest.operationType());
    }

    @DisplayName("쿠폰 사용 취소 요청을 보내는지 확인한다.")
    @Test
    void receiveCouponUpdateRequestInRollback() {
        // given
        doNothing().when(securityContextUtils).configureSecurityContext(any(Message.class));

        // when
        couponConsumer.receiveCouponUpdateRequestInRollback(rollbackRequest, message);

        // then
        verify(couponAdaptor, times(1)).updateCouponStatus(rollbackRequest.couponId(),
            rollbackRequest.operationType());
    }

    @DisplayName("쿠폰 사용이나 롤백 도중 예외가 발생할 경우, 리커버가 실행되는지 확인한다.")
    @Test
    void couponUseRecover() {
        // given
        Throwable throwable = new RuntimeException("Test Exception");
        doNothing().when(messageProducer).sendDlxMessage(anyString(), anyString(), any());

        // when
        couponConsumer.couponUseRecover(throwable, useRequest);
        couponConsumer.couponUseRecover(throwable, rollbackRequest);

        // then
        verify(messageProducer, times(1)).sendDlxMessage("dlxExchange", "dlx.couponUsedQueue", useRequest);
        verify(messageProducer, times(1)).sendDlxMessage("dlxExchange", "dlx.couponUnusedQueue", rollbackRequest);
    }
}