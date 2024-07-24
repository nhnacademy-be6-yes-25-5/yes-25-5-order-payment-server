package com.yes25.yes255orderpaymentserver.application.service.strategy.status.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateRefundRequest;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PaymentDetail;
import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.ShippingPolicyRepository;
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
class ReturnStatusStrategyTest {

    @Mock
    private UserAdaptor userAdaptor;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private ShippingPolicyRepository shippingPolicyRepository;

    @InjectMocks
    private ReturnStatusStrategy returnStatusStrategy;

    private OrderStatus orderStatus;
    private Takeout takeout;
    private Order order;
    private Payment payment;
    private List<OrderBook> orderBooks;
    private ShippingPolicy shippingPolicy;

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
            .payment(payment)
            .build();

        payment = Payment.builder()
            .paymentId(1L)
            .preOrderId("order-1234")
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

        shippingPolicy = ShippingPolicy.builder()
            .shippingPolicyId(1L)
            .shippingPolicyFee(BigDecimal.valueOf(3000))
            .shippingPolicyIsMemberOnly(false)
            .shippingPolicyIsReturnPolicy(true)
            .shippingPolicyMinAmount(BigDecimal.ZERO)
            .build();
    }

    @DisplayName("완료된 주문에 대해 반품이 성공적으로 이루어지는지 확인한다.")
    @Test
    void updateOrderStatusByOrderIdWhenRequestIsReturn() {
        // given
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        when(shippingPolicyRepository.findByShippingPolicyIsReturnPolicyTrue()).thenReturn(Optional.of(shippingPolicy));
        UpdateOrderRequest updateOrderRequest = new UpdateOrderRequest(OrderStatusType.RETURN, PaymentProvider.TOSS);

        // when
        returnStatusStrategy.updateOrderStatus(order, updateOrderRequest);

        // then
        verify(userAdaptor, times(1)).updatePointByRefund(any(UpdateRefundRequest.class));
    }

    @DisplayName("반품 요청 시 배송이 시작되지 않았으면 예외를 던진다.")
    @Test
    void handleReturnRequest_NoDeliveryStarted() {
        // given
        order = Order.builder()
            .orderId("order-1234")
            .orderCreatedAt(LocalDateTime.now())
            .orderDeliveryAt(LocalDate.now().plusDays(3))
            .customerId(1L)
            .orderTotalAmount(BigDecimal.valueOf(10000))
            .orderStatus(orderStatus)
            .payment(payment)
            .purePrice(BigDecimal.valueOf(30000))
            .takeout(takeout)
            .orderBooks(orderBooks)
            .build();

        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.RETURN, PaymentProvider.TOSS);

        // when & then
        assertThrows(AccessDeniedException.class, () -> returnStatusStrategy.updateOrderStatus(order, request));
    }
}