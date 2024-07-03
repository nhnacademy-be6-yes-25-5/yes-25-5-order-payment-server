package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.utils.AsyncSecurityContextUtils;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
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
class BookStockConsumerTest {

    @Mock
    private BookAdaptor bookAdaptor;

    @Mock
    private AsyncSecurityContextUtils securityContextUtils;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private BookStockConsumer bookStockConsumer;

    private StockRequest increaseRequest;
    private StockRequest decreaseRequest;
    private Message message;

    @BeforeEach
    void setUp() {
        increaseRequest = StockRequest.of(
            List.of(1L), List.of(1), OperationType.INCREASE
        );

        decreaseRequest = StockRequest.of(
            List.of(1L), List.of(1), OperationType.DECREASE
        );

        message = new Message(new byte[0]);
    }

    @DisplayName("도서 서버에 재고 감소 요청을 보내는지 확인한다.")
    @Test
    void receiveStockDecreaseQueue() {
        // given
        doNothing().when(securityContextUtils).configureSecurityContext(any(Message.class));

        // when
        bookStockConsumer.receiveStockDecreaseQueue(decreaseRequest, message);

        // then
        verify(bookAdaptor, times(1)).updateStock(decreaseRequest);
    }

    @DisplayName("도서 서버에 재고 증가 요청을 보내는지 확인한다.")
    @Test
    void receiveStockIncreaseQueue() {
        // given
        doNothing().when(securityContextUtils).configureSecurityContext(any(Message.class));

        // when
        bookStockConsumer.receiveStockIncreaseQueue(increaseRequest, message);

        // then
        verify(bookAdaptor, times(1)).updateStock(increaseRequest);
    }

    @DisplayName("재고 증가나 감소에 실패했을 경우, 리커버가 동작하는지 확인한다.")
    @Test
    void cartDecreaseRecover() {
        // given
        Throwable throwable = new RuntimeException("Test Exception");
        doNothing().when(messageProducer).sendDlxMessage(anyString(), anyString(), any());

        // when
        bookStockConsumer.cartRecover(throwable, increaseRequest);
        bookStockConsumer.cartRecover(throwable, decreaseRequest);

        // then
        verify(messageProducer, times(1)).sendDlxMessage("dlxExchange", "dlx.stockIncreaseQueue", increaseRequest);
        verify(messageProducer, times(1)).sendDlxMessage("dlxExchange", "dlx.stockDecreaseQueue", decreaseRequest);
    }
}