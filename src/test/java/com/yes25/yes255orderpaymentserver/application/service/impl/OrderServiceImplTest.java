package com.yes25.yes255orderpaymentserver.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.ReadPurePriceResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderCoupon;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.TakeoutType;
import com.yes25.yes255orderpaymentserver.persistance.repository.DeliveryRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderCouponRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.TakeoutRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDeliveryResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDetailResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TakeoutRepository takeoutRepository;

    @Mock
    private OrderBookRepository orderBookRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private BookAdaptor bookAdaptor;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private OrderCouponRepository orderCouponRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private PreOrder preOrder;
    private OrderStatus orderStatus;
    private Takeout takeout;
    private Order order;
    private Payment payment;
    private List<OrderBook> orderBooks;
    private SuccessPaymentResponse response;
    private ReadBookResponse readBookResponse;
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
            .paymentProvider(PaymentProvider.TOSS.name().toLowerCase())
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

        readBookResponse = ReadBookResponse.builder()
            .bookAuthor("asd")
            .bookImage("asd")
            .bookPrice(BigDecimal.valueOf(10000))
            .bookQuantity(1000)
            .bookName("zxc")
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

    @DisplayName("주문을 성공적으로 확정하는지 확인한다.")
    @Test
    void createOrder() {
        // given
        BigDecimal purePrice = BigDecimal.valueOf(30000);
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));
        when(takeoutRepository.findByTakeoutName(anyString())).thenReturn(Optional.of(takeout));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderBookRepository.saveAll(anyList())).thenReturn(orderBooks);
        when(paymentRepository.findByPreOrderId(anyString())).thenReturn(Optional.of(payment));

        // when
        orderService.createOrder(preOrder, purePrice, response);

        // then
        verify(orderStatusRepository, times(1)).findByOrderStatusName(anyString());
    }

    @DisplayName("유저 아이디를 통해 주문 내역을 확인한다.")
    @Test
    void findByUserId() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findAllByCustomerIdOrderByOrderCreatedAtDesc(1L, pageable)).thenReturn(orders);
        when(orderBookRepository.findByOrder(any(Order.class))).thenReturn(orderBooks);

        // when
        Page<ReadUserOrderAllResponse> responses = orderService.findByUserId(1L, pageable);

        // then
        assertThat(responses).isNotNull();
    }

    @DisplayName("주문 아이디와 유저 아이디를 통해 주문 상세 내역을 확인한다.")
    @Test
    void findByOrderIdAndUserId() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderBookRepository.findByOrder(any(Order.class))).thenReturn(orderBooks);

        // when
        ReadUserOrderResponse readUserOrderResponse = orderService.findByOrderIdAndUserId("order-12345", 1L);

        // then
        assertThat(readUserOrderResponse).isNotNull();
    }

    @DisplayName("3개월치 순수 주문금액을 계산하는지 확인한다.")
    @Test
    void getPurePriceByDate() {
        // given
        LocalDate now = LocalDate.now();
        List<Order> orders = List.of(order);
        when(orderRepository.findAllByOrderCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(orders);
        when(orderRepository.findAllByCustomerId(anyLong())).thenReturn(orders);
        when(orderRepository.findAllByCustomerIdAndOrderStatusOrderStatusName(anyLong(), anyString())).thenReturn(orders);

        // when
        List<ReadPurePriceResponse> responses = orderService.getPurePriceByDate(now);

        // then
        assertThat(responses).isNotNull();
    }

    @DisplayName("주문 ID로 상세 조회에 성공하는지 확인한다.")
    @Test
    void findAllOrderByOrderId() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderBookRepository.findByOrder(any(Order.class))).thenReturn(orderBooks);
        when(bookAdaptor.findBookById(anyLong())).thenReturn(readBookResponse);

        // when
        List<ReadPaymentOrderResponse> responses = orderService.findAllOrderByOrderId("order");

        // then
        assertThat(responses).isNotNull();
    }

    @DisplayName("주문 상태 조회에 성공하는지 확인한다.")
    @Test
    void findOrderStatusByOrderId() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));

        // when
        ReadOrderStatusResponse response = orderService.findOrderStatusByOrderId("order");

        // then
        assertThat(response).isNotNull();
    }

    @DisplayName("주문 상태를 '완료'로 갱신하는지 확인한다.")
    @Test
    void updateOrderStatusToDone() {
        // given
        List<Order> orders = List.of(order);
        when(orderRepository.findByOrderStatusOrderStatusNameAndDeliveryStartedAtBefore(anyString(), any(LocalDateTime.class))).thenReturn(orders);
        when(orderStatusRepository.findByOrderStatusName(anyString())).thenReturn(Optional.of(orderStatus));

        // when
        orderService.updateOrderStatusToDone();

        // then
        verify(orderRepository, times(1)).findByOrderStatusOrderStatusNameAndDeliveryStartedAtBefore(anyString(), any(LocalDateTime.class));
    }

    @DisplayName("주문 상세 조회가 성공하는지 확인한다.")
    @Test
    void getByOrderIdAndUserId() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderBookRepository.findByOrder(any(Order.class))).thenReturn(orderBooks);
        when(bookAdaptor.findBookById(anyLong())).thenReturn(readBookResponse);
        when(deliveryRepository.findAllByOrderOrderByTimestampDesc(any())).thenReturn(List.of());

        // when
        ReadOrderDeliveryResponse response = orderService.getByOrderIdAndUserId("order");

        // then
        assertThat(response).isNotNull();
    }

    @DisplayName("유저 ID와 주문 ID로 주문 상세 조회에 성공하는지 확인한다.")
    @Test
    void getOrderByOrderId() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));

        // when
        ReadOrderDetailResponse response = orderService.getOrderByOrderId("order", 1L);

        // then
        assertThat(response).isNotNull();
    }

    @DisplayName("유저 ID와 주문 ID로 주문 상세 조회할 때, 환불 정보가 있는지 확인한다.")
    @Test
    void getOrderByOrderIdHasRefund() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(refundRepository.existsByOrder(any(Order.class))).thenReturn(true);
        when(refundRepository.findByOrder_OrderId(anyString())).thenReturn(Optional.of(refund));

        // when
        ReadOrderDetailResponse response = orderService.getOrderByOrderId("order", 1L);

        // then
        assertThat(response).isNotNull();
    }

    @DisplayName("유저 ID와 주문 ID로 주문 상세 조회할 때, 주문은 했는데 환불 정보가 없다면 예외를 발생시키는지 확인한다.")
    @Test
    void getOrderByOrderIdHasNotRefund() {
        // given
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(refundRepository.existsByOrder(any(Order.class))).thenReturn(true);
        when(refundRepository.findByOrder_OrderId(anyString())).thenReturn(Optional.empty());

        // when && then
        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderByOrderId("order", 1L));
    }

    @DisplayName("이메일과 주문 ID로 주문 상세 조회에 성공하는지 확인한다.")
    @Test
    void getOrderByOrderIdAndEmail() {
        // given
        when(orderRepository.findByOrderIdAndOrderUserEmail(anyString(), anyString())).thenReturn(Optional.of(order));

        // when
        ReadOrderDetailResponse response = orderService.getOrderByOrderIdAndEmailForNoneMember("order", "email");

        // then
        assertThat(response).isNotNull();
    }

    @DisplayName("유저 ID와 책 ID로 주문 이력이 있는지 확인한다.")
    @Test
    void existOrderHistoryByUserIdAndBookId() {
        // given
        List<Order> orders = List.of(order);
        when(orderRepository.findAllByCustomerId(anyLong())).thenReturn(orders);

        // when
        Boolean exists = orderService.existOrderHistoryByUserIdAndBookId(1L, 1L);

        // then
        assertThat(exists).isTrue();
    }
}