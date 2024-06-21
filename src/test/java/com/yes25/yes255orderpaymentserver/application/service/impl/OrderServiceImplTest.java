package com.yes25.yes255orderpaymentserver.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.TakeoutType;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.TakeoutRepository;
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
    private TakeoutRepository takeoutRepository;

    @Mock
    private OrderBookRepository orderBookRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private PreOrder preOrder;
    private OrderStatus orderStatus;
    private Takeout takeout;
    private Order order;
    private List<OrderBook> orderBooks;
    private SuccessPaymentResponse response;

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
            .build();

        order = Order.builder()
            .orderId("order")
            .orderCreatedAt(LocalDateTime.now())
            .orderDeliveryAt(LocalDate.now().plusDays(3))
            .orderTotalAmount(BigDecimal.valueOf(10000))
            .orderStatus(orderStatus)
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
            .couponId(1L)
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
            .orderProductQuantity(2)
            .price(BigDecimal.valueOf(1000))
            .build();

        orderBooks = List.of(orderBook);
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
}