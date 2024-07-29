package com.yes25.yes255orderpaymentserver.application.service.strategy.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.TossAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderCoupon;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PaymentDetail;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentDetailRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentRetryServiceTest {

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private TossAdaptor tossAdaptor;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDetailRepository paymentDetailRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    private PaymentRetryService paymentRetryService;
    private CreatePaymentRequest createPaymentRequest;
    private String authorizations;
    private JSONObject object;
    private int attempt;
    private int maxAttempt;
    private int retryDelayMs;

    @BeforeEach
    void setUp() {
        paymentRetryService = new PaymentRetryService(messageProducer, tossAdaptor, paymentRepository, paymentDetailRepository, orderStatusRepository);

        createPaymentRequest = CreatePaymentRequest.builder()
            .paymentKey("key")
            .amount("10000")
            .paymentProvider(PaymentProvider.TOSS)
            .bookIds(List.of(1L))
            .orderId("order")
            .quantities(List.of(1))
            .build();

        object = new JSONObject();

        authorizations = "auth";
        attempt = 0;
        maxAttempt = 3;
        retryDelayMs = 100;
    }

    @DisplayName("결제 재시도를 성공적으로 수행하는지 확인한다.")
    @Test
    void retryPaymentConfirmSuccess() {
        // given
        JSONObject responseObject = new JSONObject();
        responseObject.put("approvedAt", "2023-07-01T10:15:30");
        responseObject.put("requestedAt", "2023-07-01T10:10:30");
        responseObject.put("totalAmount", 10000);
        responseObject.put("method", "CARD");

        when(tossAdaptor.confirmPayment(anyString(), anyString())).thenReturn(responseObject);

        Payment payment = mock(Payment.class);

        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));

        // when
        paymentRetryService.retryPaymentConfirm(createPaymentRequest, authorizations, object, attempt, 1L, maxAttempt, retryDelayMs);

        // then
        verify(tossAdaptor, times(1)).confirmPayment(anyString(), anyString());
        verify(paymentRepository, times(1)).findById(anyLong());
        verify(paymentDetailRepository, times(1)).save(any(PaymentDetail.class));
    }

    @DisplayName("결제 정보를 찾을 수 없을 경우 예외를 반환하는지 확인한다.")
    @Test
    void retryPaymentConfirmFailureWhenPaymentNotFound() {
        // given
        JSONObject responseObject = new JSONObject();
        responseObject.put("approvedAt", "2023-07-01T10:15:30");
        responseObject.put("requestedAt", "2023-07-01T10:10:30");
        responseObject.put("totalAmount", 10000);
        responseObject.put("method", "CARD");

        when(tossAdaptor.confirmPayment(anyString(), anyString())).thenReturn(responseObject);
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when && then
        assertThatThrownBy(() ->
            paymentRetryService.retryPaymentConfirm(createPaymentRequest, authorizations, object, attempt, 1L, maxAttempt, retryDelayMs)
        ).isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("결제 재시도가 중단 예외(InterruptedException)에 의해 실패하고 적절하게 처리되는지 확인한다.")
    @Test
    void retryPaymentConfirmFailureWhenInterruptedException() {
        // given && when
        Thread testThread = new Thread(() -> {
            paymentRetryService.retryPaymentConfirm(createPaymentRequest, authorizations, object, attempt, 1L, maxAttempt, retryDelayMs);
        });

        testThread.start();
        testThread.interrupt();

        try {
            testThread.join();
        } catch (InterruptedException ignore) {}

        // then
        verify(tossAdaptor, never()).confirmPayment(anyString(), anyString());
        verify(messageProducer, never()).sendOrderCancelMessageByUser(anyList(), anyList(), anyList(), any(BigDecimal.class), any(BigDecimal.class));
        verify(paymentDetailRepository, never()).save(any(PaymentDetail.class));
    }


    @DisplayName("결제 재시도가 실패하고 최종적으로 처리되는지 확인한다.")
    @Test
    void retryPaymentConfirmFailure() {
        // given
        when(tossAdaptor.confirmPayment(anyString(), anyString())).thenThrow(new RuntimeException("Payment failed"));

        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);
        OrderStatus orderStatus = mock(OrderStatus.class);

        when(order.getPoints()).thenReturn(BigDecimal.valueOf(1000));
        when(order.getPurePrice()).thenReturn(BigDecimal.valueOf(1000));

        when(payment.getOrder()).thenReturn(order);

        OrderBook orderBook = mock(OrderBook.class);
        when(orderBook.getBookId()).thenReturn(1L);
        when(orderBook.getOrderBookQuantity()).thenReturn(1);

        OrderCoupon orderCoupon = mock(OrderCoupon.class);
        when(orderCoupon.getUserCouponId()).thenReturn(1L);

        when(order.getOrderBooks()).thenReturn(List.of(orderBook));
        when(order.getOrderCoupons()).thenReturn(List.of(orderCoupon));

        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));

        // when
        paymentRetryService.retryPaymentConfirm(createPaymentRequest, authorizations, object, attempt, 1L, maxAttempt, retryDelayMs);

        // then
        verify(tossAdaptor, times(maxAttempt)).confirmPayment(anyString(), anyString());
        verify(messageProducer, times(1)).sendOrderCancelMessageByUser(anyList(), anyList(), anyList(), any(BigDecimal.class), any(BigDecimal.class));
        verify(paymentDetailRepository, never()).save(any(PaymentDetail.class));
    }

    @DisplayName("주문 상태를 업데이트할 때, 주문 상태를 찾을 수 없으면 예외를 반환하는지 확인한다.")
    @Test
    void retryPaymentConfirmFailureWhenOrderStatusNotFound() {
        // given
        when(tossAdaptor.confirmPayment(anyString(), anyString())).thenThrow(new RuntimeException("Payment failed"));

        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);

        when(order.getPoints()).thenReturn(BigDecimal.valueOf(1000));
        when(order.getPurePrice()).thenReturn(BigDecimal.valueOf(1000));

        when(payment.getOrder()).thenReturn(order);

        OrderBook orderBook = mock(OrderBook.class);
        when(orderBook.getBookId()).thenReturn(1L);
        when(orderBook.getOrderBookQuantity()).thenReturn(1);

        OrderCoupon orderCoupon = mock(OrderCoupon.class);
        when(orderCoupon.getUserCouponId()).thenReturn(1L);

        when(order.getOrderBooks()).thenReturn(List.of(orderBook));
        when(order.getOrderCoupons()).thenReturn(List.of(orderCoupon));

        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() ->
        paymentRetryService.retryPaymentConfirm(createPaymentRequest, authorizations, object, attempt, 1L, maxAttempt, retryDelayMs)
        ).isInstanceOf(EntityNotFoundException.class);
    }
}