package com.yes25.yes255orderpaymentserver.application.service.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.service.strategy.status.OrderStatusStrategyProvider;
import com.yes25.yes255orderpaymentserver.application.service.strategy.status.impl.CancelStatusStrategy;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderStatusContextTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusStrategyProvider orderStatusStrategyProvider;

    @Mock
    private CancelStatusStrategy cancelStatusStrategy;

    @InjectMocks
    private OrderStatusContext orderStatusContext;

    private OrderStatus orderStatus;
    private Takeout takeout;
    private Order order;
    private Payment payment;

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

        payment = Payment.builder()
            .paymentId(1L)
            .preOrderId("order-1234")
            .paymentKey("dsadsad")
            .paymentAmount(BigDecimal.valueOf(10000))
            .paymentMethod("카드")
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
            .build();
    }

    @DisplayName("주문 상태를 갱신할 때 주문을 찾지 못하면 예외를 던진다.")
    @Test
    void updateOrderStatusByOrderId_OrderNotFound() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.empty());
        UpdateOrderRequest request = mock(UpdateOrderRequest.class);

        // when & then
        assertThrows(EntityNotFoundException.class, () -> orderStatusContext.updateOrderStatusByOrderId("order-1234", request, 1L));
    }

    @DisplayName("주문 상태를 갱신할 때, 사용자 정보가 다르면 예외를 던진다.")
    @Test
    void updateOrderStatusByOrderId_DifferentUserInfo() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        UpdateOrderRequest request = mock(UpdateOrderRequest.class);

        // when & then
        assertThrows(AccessDeniedException.class, () -> orderStatusContext.updateOrderStatusByOrderId("order-1234", request, 3L));
    }

    @DisplayName("주문 상태를 성공적으로 변경하는지 확인한다.")
    @Test
    void updateOrderStatusByOrderId_OrderStatusNotFound() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusStrategyProvider.getStrategy(anyString())).thenReturn(cancelStatusStrategy);
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.CANCEL, PaymentProvider.TOSS);

        // when
        UpdateOrderResponse response = orderStatusContext.updateOrderStatusByOrderId(
            order.getOrderId(), request, 1L);

        // then
        assertThat(response).isNotNull();
    }
}