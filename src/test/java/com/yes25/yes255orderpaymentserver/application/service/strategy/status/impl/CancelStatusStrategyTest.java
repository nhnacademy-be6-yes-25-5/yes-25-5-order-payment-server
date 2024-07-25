package com.yes25.yes255orderpaymentserver.application.service.strategy.status.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.service.context.PaymentContext;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PaymentDetail;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelStatusStrategyTest {

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private OrderBookRepository orderBookRepository;

    @Mock
    private PaymentContext paymentContext;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private CancelStatusStrategy cancelStatusStrategy;

    private OrderStatus orderStatus;
    private Takeout takeout;
    private Order order;
    private Payment payment;
    private List<OrderBook> orderBooks;

    @BeforeEach
    void setUp() {
        orderStatus = OrderStatus.builder()
            .orderStatusId(1L)
            .orderStatusName("WAIT")
            .build();

        takeout = Takeout.builder()
            .takeoutId(1L)
            .takeoutDescription("없음")
            .takeoutName("NONE")
            .takeoutPrice(BigDecimal.valueOf(10000))
            .build();

        OrderBook orderBook = OrderBook.builder()
            .orderBookId(1L)
            .bookId(1L)
            .orderBookQuantity(2)
            .price(BigDecimal.valueOf(1000))
            .build();

        orderBooks = List.of(orderBook);

        PaymentDetail paymentDetail = PaymentDetail.builder()
            .paymentDetailId(1L)
            .paymentAmount(BigDecimal.ZERO)
            .paymentMethod("카드")
            .build();

        payment = Payment.builder()
            .paymentId(1L)
            .paymentKey("dsadsad")
            .paymentDetail(paymentDetail)
            .build();

        order = Order.builder()
            .orderId("order-1234")
            .orderCreatedAt(LocalDateTime.now())
            .orderDeliveryAt(LocalDate.now().plusDays(3))
            .deliveryStartedAt(LocalDateTime.now().plusDays(1))
            .customerId(1L)
            .orderTotalAmount(BigDecimal.valueOf(10000))
            .orderStatus(orderStatus)
            .payment(payment)
            .purePrice(BigDecimal.valueOf(30000))
            .takeout(takeout)
            .orderBooks(orderBooks)
            .build();
    }

    @Test
    @DisplayName("대기중인 주문에 대해 결제 취소가 성공적으로 이루어지는지 확인한다.")
    void updateOrderStatusByOrderIdWhenRequestIsCancel() {
        // given
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        when(orderBookRepository.findByOrder(any(Order.class))).thenReturn(orderBooks);
        UpdateOrderRequest updateOrderRequest = new UpdateOrderRequest(OrderStatusType.CANCEL, PaymentProvider.TOSS);

        // when
        cancelStatusStrategy.updateOrderStatus(order, updateOrderRequest);

        // then
        verify(messageProducer, times(1)).sendOrderCancelMessageByUser(anyList(), anyList(), anyList(), any(), any());
    }

    @DisplayName("주문을 취소할 때 대기중이 아니면 예외를 던진다.")
    @Test
    void handleCancelRequest_NotWait() {
        // given
        orderStatus = OrderStatus.builder()
            .orderStatusId(2L)
            .orderStatusName("DELIVERING")
            .build();

        order = Order.builder()
            .orderStatus(orderStatus)
            .build();

        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.CANCEL, PaymentProvider.TOSS);

        // when & then
        assertThrows(AccessDeniedException.class, () -> cancelStatusStrategy.updateOrderStatus(order, request));
    }
}