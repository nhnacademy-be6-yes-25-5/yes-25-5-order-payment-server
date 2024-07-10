package com.yes25.yes255orderpaymentserver.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.service.PaymentProcessor;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderStatusNotFoundException;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Delivery;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.repository.DeliveryRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundStatusRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CancelOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderStatusRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CancelOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllUserOrderCancelStatusResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private RefundStatusRepository refundStatusRepository;

    @Mock
    private PaymentProcessor paymentProcessor;

    @Mock
    private BookAdaptor bookAdaptor;

    @InjectMocks
    private AdminOrderServiceImpl adminOrderService;

    private Order order;
    private Payment payment;
    private Refund refund;
    private Pageable pageable;
    private Delivery delivery;
    private OrderBook orderBook;
    private OrderStatus orderStatus;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
            .paymentId(1L)
            .paymentMethod("카드")
            .preOrderId("1")
            .paymentKey("qwer")
            .paymentAmount(BigDecimal.valueOf(10000))
            .build();

        orderBook = OrderBook.builder()
            .bookId(1L)
            .orderBookId(1L)
            .build();

        orderStatus = OrderStatus.builder()
            .orderStatusName("WAIT")
            .orderStatusId(1L)
            .build();

        order = Order.builder()
            .orderId("1")
            .orderBooks(List.of(orderBook))
            .orderStatus(orderStatus)
            .userRole("MEMBER")
            .payment(payment)
            .build();

        refund = Refund.builder()
            .order(order)
            .build();

        pageable = PageRequest.of(0, 10);

        delivery = Delivery.builder()
            .deliveryId(1L)
            .timestamp(LocalDateTime.now())
            .deliveryStatus("WAIT")
            .order(order)
            .build();
    }

    @Test
    @DisplayName("모든 주문을 페이징 처리하여 가져올 때, 올바르게 가져오는지 확인한다.")
    void getAllOrdersByPaging() {
        // given
        Page<Order> orders = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findAllByOrderByOrderCreatedAtDesc(pageable)).thenReturn(orders);

        // when
        Page<ReadAllOrderResponse> result = adminOrderService.getAllOrdersByPaging(pageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository, times(1)).findAllByOrderByOrderCreatedAtDesc(pageable);
    }

    @ParameterizedTest
    @DisplayName("모든 주문을 페이징 처리하여 가져올 때, 회원, 비회원을 구분하여 가져오는지 확인한다.")
    @ValueSource(strings = {"MEMBER", "NONE_MEMBER"})
    void getAllOrdersByPagingCheckRole(String role) {
        // given
        Page<Order> orders = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findAllByUserRoleOrderByOrderCreatedAtDesc(role, pageable)).thenReturn(
            orders);

        // when
        Page<ReadAllOrderResponse> result = adminOrderService.getAllOrdersByPaging(pageable, role);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository, times(1)).findAllByUserRoleOrderByOrderCreatedAtDesc(role, pageable);
    }


    @Test
    @DisplayName("취소된 모든 주문을 페이징 처리하여 가져올 때, 올바르게 가져오는지 확인한다.")
    void getAllCancelOrdersByPaging() {
        // given
        Page<Refund> refunds = new PageImpl<>(List.of(refund), pageable, 1);
        when(refundRepository.findAllByRefundStatus_RefundStatusName(anyString(),
            any(Pageable.class))).thenReturn(refunds);

        // when
        Page<ReadAllUserOrderCancelStatusResponse> result = adminOrderService.getAllCancelOrdersByPaging(
            pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(refundRepository, times(1)).findAllByRefundStatus_RefundStatusName(anyString(),
            any(Pageable.class));
    }

    @Test
    @DisplayName("주문을 취소할 때, 올바르게 취소되는지 확인한다.")
    void cancelOrderByOrderId() {
        // given
        CancelOrderRequest request = mock(CancelOrderRequest.class);
        RefundStatus refundStatus = RefundStatus.builder().build();
        when(refundRepository.findByOrder_OrderId(anyString())).thenReturn(Optional.of(refund));
        when(refundStatusRepository.findByRefundStatusName(anyString())).thenReturn(
            Optional.of(refundStatus));
        when(request.status()).thenReturn(CancelStatus.ACCESS);

        // when
        CancelOrderResponse response = adminOrderService.cancelOrderByOrderId("1", request);

        // then
        assertThat(response).isNotNull();
        verify(refundRepository, times(1)).findByOrder_OrderId(anyString());
        verify(refundStatusRepository, times(1)).findByRefundStatusName(anyString());
        verify(paymentProcessor, times(1)).cancelPayment(anyString(), anyString(), anyInt(),
            anyString());
    }

    @Test
    @DisplayName("주문을 찾지 못할 때, 예외를 던지는지 확인한다.")
    void updateOrderStatusByOrderId_OrderNotFound() {
        // given
        UpdateOrderStatusRequest request = mock(UpdateOrderStatusRequest.class);
        when(orderRepository.findById(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThrows(OrderNotFoundException.class,
            () -> adminOrderService.updateOrderStatusByOrderId("1", request));
    }

    @Test
    @DisplayName("주문 상태를 찾지 못할 때, 예외를 던지는지 확인한다.")
    void updateOrderStatusByOrderId_OrderStatusNotFound() {
        // given
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatusType.WAIT);
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThrows(OrderStatusNotFoundException.class,
            () -> adminOrderService.updateOrderStatusByOrderId("1", request));
    }

    @Test
    @DisplayName("환불 정보를 찾지 못할 때, 예외를 던지는지 확인한다.")
    void cancelOrderByOrderId_RefundNotFound() {
        // given
        CancelOrderRequest request = mock(CancelOrderRequest.class);
        when(refundRepository.findByOrder_OrderId(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
            () -> adminOrderService.cancelOrderByOrderId("1", request));
    }

    @Test
    @DisplayName("환불 상태를 찾지 못할 때, 예외를 던지는지 확인한다.")
    void cancelOrderByOrderId_RefundStatusNotFound() {
        // given
        CancelOrderRequest request = new CancelOrderRequest(CancelStatus.NONE);
        when(refundRepository.findByOrder_OrderId(anyString())).thenReturn(Optional.of(refund));
        when(refundStatusRepository.findByRefundStatusName(anyString())).thenReturn(
            Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
            () -> adminOrderService.cancelOrderByOrderId("1", request));
    }
}
