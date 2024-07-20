package com.yes25.yes255orderpaymentserver.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.context.PaymentContext;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderStatusNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.PaymentNotFoundException;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Delivery;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderCoupon;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.TakeoutType;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.ShippingPolicyRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;
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
class OrderStatusServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private RefundStatusRepository refundStatusRepository;

    @Mock
    private ShippingPolicyRepository shippingPolicyRepository;

    @Mock
    private OrderBookRepository orderBookRepository;

    @Mock
    private PaymentContext paymentContext;

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private UserAdaptor userAdaptor;

    @InjectMocks
    private OrderStatusServiceImpl orderStatusService;

    private PreOrder preOrder;
    private OrderStatus orderStatus;
    private Takeout takeout;
    private Order order;
    private Payment payment;
    private List<OrderBook> orderBooks;
    private SuccessPaymentResponse response;
    private ShippingPolicy shippingPolicy;
    private ReadBookResponse readBookResponse;
    private Delivery delivery;
    private Refund refund;
    private OrderCoupon orderCoupon;

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

        preOrder = PreOrder.builder()
            .preOrderId("preOrder-12345")
            .bookIds(List.of(1L, 2L, 3L))
            .quantities(List.of(1, 2, 1))
            .prices(List.of(new BigDecimal("10000"), new BigDecimal("20000"), new BigDecimal("15000")))
            .userId(1L)
            .orderTotalAmount(new BigDecimal("45000"))
            .discountPrice(new BigDecimal("5000"))
            .points(new BigDecimal("1000"))
            .takeoutPrice(new BigDecimal("0"))
            .shippingFee(new BigDecimal("3000"))
            .takeoutType(TakeoutType.NONE)
            .addressRaw("123 Main St")
            .addressDetail("Apt 4B")
            .zipcode("12345")
            .reference("Near the park")
            .orderedDate(LocalDateTime.now())
            .deliveryDate(LocalDate.now().plusDays(3))
            .orderUserName("John Doe")
            .orderUserEmail("john.doe@example.com")
            .orderUserPhoneNumber("123-456-7890")
            .receiveName("Jane Doe")
            .receiveEmail("jane.doe@example.com")
            .receivePhoneNumber("098-765-4321")
            .couponIds(List.of(1L))
            .build();

        response = SuccessPaymentResponse.builder()
            .bookIdList(List.of(1L, 2L))
            .orderId("order-12345")
            .paymentAmount(50000)
            .paymentKey("key")
            .quantityList(List.of(1, 2))
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
            .orderBooks(orderBooks)
            .build();

        shippingPolicy = ShippingPolicy.builder()
            .shippingPolicyId(1L)
            .shippingPolicyFee(BigDecimal.valueOf(3000))
            .shippingPolicyIsMemberOnly(false)
            .shippingPolicyIsReturnPolicy(true)
            .shippingPolicyMinAmount(BigDecimal.ZERO)
            .build();

        readBookResponse = ReadBookResponse.builder()
            .bookAuthor("asd")
            .bookImage("asd")
            .bookPrice(BigDecimal.valueOf(10000))
            .bookQuantity(1000)
            .bookName("zxc")
            .build();

        delivery = Delivery.builder()
            .order(order)
            .deliveryStatus("WAIT")
            .timestamp(LocalDateTime.now())
            .deliveryId(1L)
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

        orderCoupon = OrderCoupon.builder()
            .userCouponId(1L)
            .order(order)
            .build();
    }


    @Test
    @DisplayName("대기중인 주문에 대해 결제 취소가 성공적으로 이루어지는지 확인한다.")
    void updateOrderStatusByOrderIdWhenRequestIsCancel() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        String orderId = "order-1234";
        Long userId = 1L;
        UpdateOrderRequest updateOrderRequest = new UpdateOrderRequest(OrderStatusType.CANCEL, PaymentProvider.TOSS);

        // when
        UpdateOrderResponse updateOrderResponse = orderStatusService.updateOrderStatusByOrderId(orderId, updateOrderRequest, userId);

        // then
        assertThat(updateOrderResponse.message()).isEqualTo("주문 상태가 성공적으로 변경되었습니다.");
    }

    @DisplayName("완료된 주문에 대해 반품이 성공적으로 이루어지는지 확인한다.")
    @Test
    void updateOrderStatusByOrderIdWhenRequestIsReturn() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        when(shippingPolicyRepository.findByShippingPolicyIsReturnPolicyTrue()).thenReturn(Optional.of(shippingPolicy));
        String orderId = "order-1234";
        Long userId = 1L;
        UpdateOrderRequest updateOrderRequest = new UpdateOrderRequest(OrderStatusType.RETURN, PaymentProvider.TOSS);

        // when
        UpdateOrderResponse updateOrderResponse = orderStatusService.updateOrderStatusByOrderId(orderId, updateOrderRequest, userId);

        // then
        assertThat(updateOrderResponse.message()).isEqualTo("주문 상태가 성공적으로 변경되었습니다.");
    }

    @DisplayName("주문 상태를 갱신할 때 주문을 찾지 못하면 예외를 던진다.")
    @Test
    void updateOrderStatusByOrderId_OrderNotFound() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.empty());
        UpdateOrderRequest request = mock(UpdateOrderRequest.class);

        // when & then
        assertThrows(OrderNotFoundException.class, () -> orderStatusService.updateOrderStatusByOrderId("order-1234", request, 1L));
    }

    @DisplayName("주문 상태를 갱신할 때 주문 상태를 찾지 못하면 예외를 던진다.")
    @Test
    void updateOrderStatusByOrderId_OrderStatusNotFound() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.empty());
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.CANCEL, PaymentProvider.TOSS);

        // when & then
        assertThrows(OrderStatusNotFoundException.class, () -> orderStatusService.updateOrderStatusByOrderId("order-1234", request, 1L));
    }

    @DisplayName("주문을 취소할 때 결제가 완료되지 않으면 예외를 던진다.")
    @Test
    void handleCancelRequest_FailedPayment() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        doThrow(new PaymentNotFoundException("order-1234")).when(paymentContext).cancelPayment(anyString(), anyString(), anyInt(), anyString(), anyString());
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.CANCEL, PaymentProvider.TOSS);

        // when & then
        assertThrows(PaymentNotFoundException.class, () -> orderStatusService.updateOrderStatusByOrderId("order-1234", request, 1L));
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

        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.RETURN, PaymentProvider.TOSS);

        // when & then
        assertThrows(AccessDeniedException.class, () -> orderStatusService.updateOrderStatusByOrderId("order-1234", request, 1L));
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

        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        when(refundStatusRepository.findByRefundStatusName(any())).thenReturn(Optional.of(refundStatus));
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.REFUND, PaymentProvider.TOSS);

        // when
        UpdateOrderResponse updateOrderResponse = orderStatusService.updateOrderStatusByOrderId("order", request, 1L);

        // then
        assertThat(updateOrderResponse).isNotNull();
    }

    @DisplayName("환불 요청 시 반품이 완료되지 않았으면 예외를 던진다.")
    @Test
    void handleRefundRequest_NotReturned() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatusType.REFUND, PaymentProvider.TOSS);

        // when & then
        assertThrows(AccessDeniedException.class, () -> orderStatusService.updateOrderStatusByOrderId("order-1234", request, 1L));
    }
}