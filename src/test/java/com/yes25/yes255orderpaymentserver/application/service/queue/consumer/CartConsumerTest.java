package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateUserCartQuantityRequest;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.utils.AsyncSecurityContextUtils;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

@ExtendWith(MockitoExtension.class)
class CartConsumerTest {

    @Mock
    private UserAdaptor userAdaptor;

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private AsyncSecurityContextUtils securityContextUtils;

    @InjectMocks
    private CartConsumer cartConsumer;

    private List<UpdateUserCartQuantityRequest> requests;

    private Message message;

    @BeforeEach
    void setUp() {
        requests = List.of(
            UpdateUserCartQuantityRequest.of(1L, 2, "cartId"),
            UpdateUserCartQuantityRequest.of(2L, 3, "cartId")
        );

        message = new Message(new byte[0]);
    }

    @DisplayName("메세지를 받아 성공적으로 도서 서버에 장바구니 도서 재고 감소 요청을 보내는지 확인한다.")
    @Test
    void receiveCartDecreaseQueue() {
        // given && when
        cartConsumer.receiveCartDecreaseQueue(requests);

        // then
        verify(userAdaptor, times(1)).decreaseUserCartQuantity(requests);
    }

    @DisplayName("장바구니 재고 감소 중 예외 발생 시, 리커버가 동작하는지 확인한다.")
    @Test
    void cartDecreaseRecover() {
        // given
        Throwable throwable = new RuntimeException("Test Exception");
        doNothing().when(messageProducer).sendDlxMessage(anyString(), anyString(), any());

        // when
        cartConsumer.cartDecreaseRecover(throwable, requests);

        // then
        verify(messageProducer, times(1)).sendDlxMessage("dlxExchange", "dlx.cartDecreaseQueue", requests);
    }
}