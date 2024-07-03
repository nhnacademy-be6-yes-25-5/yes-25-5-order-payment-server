package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.utils.AsyncSecurityContextUtils;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

@ExtendWith(MockitoExtension.class)
class PointConsumerTest {

    @Mock
    private UserAdaptor userAdaptor;

    @Mock
    private AsyncSecurityContextUtils securityContextUtils;

    @Mock
    private Message message;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private PointConsumer pointConsumer;

    private UpdatePointMessage useMessage;
    private UpdatePointMessage rollbackMessage;

    @BeforeEach
    void setUp() {
        useMessage = UpdatePointMessage.of(BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), OperationType.USE);
        rollbackMessage = UpdatePointMessage.of(BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), OperationType.ROLLBACK);

        message = new Message(new byte[1024]);
    }

    @DisplayName("회원 서버에 요청을 보내 포인트를 적립 및 차감하는지 확인한다.")
    @Test
    void receiveUsedPoints() {
        // given
        doNothing().when(securityContextUtils).configureSecurityContext(any(Message.class));
        UpdatePointRequest request = UpdatePointRequest.from(useMessage);

        // when
        pointConsumer.receiveUsedPoints(useMessage, message);

        // then
        verify(userAdaptor, times(1)).updatePoint(request);
    }

    @DisplayName("회원 서버에 요청을 보내 포인트를 적립 및 차감을 롤백하는지 확인한다.")
    @Test
    void receiveUnusedPoints() {
        // given
        doNothing().when(securityContextUtils).configureSecurityContext(any(Message.class));
        UpdatePointRequest request = UpdatePointRequest.from(rollbackMessage);

        // when
        pointConsumer.receiveUsedPoints(rollbackMessage, message);

        // then
        verify(userAdaptor, times(1)).updatePoint(request);
    }

    @DisplayName("포인트 적립 및 차감에서 예외가 발생하면 리커버가 실행되는지 확인한다.")
    @Test
    void pointRecover() {
        // given
        Throwable throwable = new RuntimeException("Test Exception");
        doNothing().when(messageProducer).sendDlxMessage(anyString(), anyString(), any());

        // when
        pointConsumer.pointRecover(throwable, useMessage);
        pointConsumer.pointRecover(throwable, rollbackMessage);

        // then
        verify(messageProducer, times(1)).sendDlxMessage("dlxExchange", "dlx.pointUsedQueue", useMessage);
        verify(messageProducer, times(1)).sendDlxMessage("dlxExchange", "dlx.pointReturnQueue", rollbackMessage);
    }
}