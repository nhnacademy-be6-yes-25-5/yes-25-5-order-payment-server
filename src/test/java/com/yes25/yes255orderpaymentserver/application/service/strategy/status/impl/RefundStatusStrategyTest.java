package com.yes25.yes255orderpaymentserver.application.service.strategy.status.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundStatusRepository;
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
class RefundStatusStrategyTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private RefundStatusRepository refundStatusRepository;

    @InjectMocks
    private RefundStatusStrategy refundStatusStrategy;

    private OrderStatus orderStatus;
    private Takeout takeout;
    private Order order;
    private Payment payment;
    private List<OrderBook> orderBooks;
    private Refund refund;

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

        payment = Payment.builder()
            .paymentId(1L)
            .preOrderId("order-1234")
            .paymentKey("dsadsad")
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
        refund = Refund.builder()
            .order(order)
            .refundId(1L)
            .refundStatus(RefundStatus.builder()
                .refundStatusId(1L)
                .refundStatusName("WAIT")
                .build())
            .requestedAt(LocalDate.now())
            .build();
    }

    @DisplayName("환불 요청을 보내는지 확인한다.")
    @Test
    void handleRefundRequest() {
        // given
        orderStatus = OrderStatus.builder()
            .orderStatusId(1L)
            .orderStatusName(OrderStatusType.REFUND.name())
            .build();

        order = Order.builder()
            .orderId("order-1234")
            .orderCreatedAt(LocalDateTime.now())
            .orderDeliveryAt(LocalDate.now().plusDays(3))
            .customerId(1L)
            .orderTotalAmount(BigDecimal.valueOf(10000))
            .orderStatus(OrderStatus.builder().orderStatusName("RETURN").build())
            .payment(payment)
            .purePrice(BigDecimal.valueOf(30000))
            .takeout(takeout)
            .orderBooks(orderBooks)
            .build();

        RefundStatus refundStatus = RefundStatus.builder()
            .refundStatusId(1L)
            .refundStatusName("WAIT")
            .build();

        when(refundStatusRepository.findByRefundStatusName(any())).thenReturn(Optional.of(refundStatus));
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.REFUND, PaymentProvider.TOSS);

        // when
        refundStatusStrategy.updateOrderStatus(order, request);

        // then
        verify(refundRepository, times(1)).save(any(Refund.class));
    }

    @DisplayName("환불 요청 시 반품이 완료되지 않았으면 예외를 던진다.")
    @Test
    void handleRefundRequest_NotReturned() {
        // given
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.REFUND, PaymentProvider.TOSS);

        // when & then
        assertThrows(AccessDeniedException.class, () -> refundStatusStrategy.updateOrderStatus(order, request));
    }
}