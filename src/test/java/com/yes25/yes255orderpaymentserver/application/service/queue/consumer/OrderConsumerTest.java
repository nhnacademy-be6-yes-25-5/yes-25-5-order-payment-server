package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.context.PaymentContext;
import com.yes25.yes255orderpaymentserver.application.service.impl.OrderServiceImpl;
import com.yes25.yes255orderpaymentserver.application.service.impl.PreOrderServiceImpl;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.exception.PaymentException;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class OrderConsumerTest {

    @Mock
    private OrderServiceImpl orderService;

    @Mock
    private PreOrderServiceImpl preOrderService;

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private PaymentContext paymentContext;

    @InjectMocks
    private OrderConsumer orderConsumer;

    private SuccessPaymentResponse successPaymentResponse;
    private Message message;

    @BeforeEach
    void setUp() {
        successPaymentResponse = new SuccessPaymentResponse(
            "orderId", "paymentKey", 10000, PaymentProvider.TOSS, List.of(1L), List.of(1));
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("Authorization", "authToken");
        message = new Message(new byte[0], messageProperties);
    }

    @DisplayName("인증헤더가 비어있다면 비회원으로 처리하는지 확인한다.")
    @Test
    void testReceiveAuthorizationIsNull(CapturedOutput output) {
        // given
        MessageProperties messageProperties = new MessageProperties();
        message = new Message(new byte[0], messageProperties);

        PreOrder preOrder = mock(PreOrder.class);
        when(preOrderService.getPreOrder(anyString())).thenReturn(preOrder);
        when(preOrder.calculatePurePrice()).thenReturn(BigDecimal.valueOf(9000));

        // when
        orderConsumer.receivePayment(successPaymentResponse, message);

        // then
        assertTrue(output.getOut().contains("인증 헤더가 비어있습니다. 비회원으로 처리합니다."));
    }

    @Test
    @DisplayName("결제 완료 후, 주문을 확정하는지 확인한다.")
    void testReceivePaymentSuccess() {
        // given
        PreOrder preOrder = mock(PreOrder.class);
        when(preOrderService.getPreOrder(anyString())).thenReturn(preOrder);
        when(preOrder.calculatePurePrice()).thenReturn(BigDecimal.valueOf(9000));

        // when
        orderConsumer.receivePayment(successPaymentResponse, message);

        // then
        verify(preOrderService).getPreOrder(successPaymentResponse.orderId());
        verify(orderService).createOrder(preOrder, BigDecimal.valueOf(9000), successPaymentResponse);
        verify(messageProducer).sendOrderDone(preOrder, BigDecimal.valueOf(9000), "authToken", preOrder.getCartId());
    }

    @Test
    @DisplayName("결제 처리 중 PreOrder가 null인 경우 예외 발생하는지 확인한다.")
    void testReceivePaymentPreOrderNull() {
        // given
        when(preOrderService.getPreOrder(anyString())).thenReturn(null);

        // when && then
        assertThrows(PaymentException.class, () -> orderConsumer.receivePayment(successPaymentResponse, message));
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 시 결제 취소 및 재고 증가 요청하는지 확인한다.")
    void testRecover() {
        // given
        ReflectionTestUtils.setField(orderConsumer, "orderService", orderService);
        ReflectionTestUtils.setField(orderConsumer, "preOrderService", preOrderService);
        ReflectionTestUtils.setField(orderConsumer, "messageProducer", messageProducer);
        ReflectionTestUtils.setField(orderConsumer, "paymentContext", paymentContext);
        StockRequest stockRequest = StockRequest.of(successPaymentResponse.bookIdList(), successPaymentResponse.quantityList(), OperationType.INCREASE);

        // when
        orderConsumer.recover(new Exception(), successPaymentResponse, message);

        // then
        verify(messageProducer).sendMessage("stockDecreaseExchange", "stockDecreaseRoutingKey", stockRequest, "authToken");
        verify(paymentContext).cancelPayment(successPaymentResponse.paymentKey(), "결제 처리 중 예상치 못한 예외 발생", successPaymentResponse.paymentAmount(), successPaymentResponse.orderId(), successPaymentResponse.paymentProvider().name().toLowerCase());
    }
}