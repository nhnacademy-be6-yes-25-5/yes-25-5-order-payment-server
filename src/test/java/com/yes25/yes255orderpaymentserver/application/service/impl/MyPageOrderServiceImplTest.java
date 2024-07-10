package com.yes25.yes255orderpaymentserver.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.dto.request.ReadBookInfoResponse;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadMyOrderHistoryResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
class MyPageOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookAdaptor bookAdaptor;

    @InjectMocks
    private MyPageOrderServiceImpl myPageOrderService;

    private Order order;
    private OrderBook orderBook;
    private ReadBookInfoResponse readBookInfoResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        orderBook = OrderBook.builder()
            .orderBookId(1L)
            .bookId(1L)
            .orderBookQuantity(2)
            .price(BigDecimal.valueOf(10000))
            .build();

        order = Order.builder()
            .orderId("1")
            .orderCreatedAt(LocalDateTime.now())
            .orderTotalAmount(BigDecimal.valueOf(20000))
            .orderBooks(List.of(orderBook))
            .orderStatus(new OrderStatus(1L, "WAIT"))
            .build();

        readBookInfoResponse = ReadBookInfoResponse.builder()
            .bookId(1L)
            .bookName("Test Book")
            .build();
    }

    @DisplayName("유저 ID로 주문 내역을 페이징 처리하여 가져오는지 확인한다.")
    @Test
    void getMyOrdersByPaging() {
        // given
        Page<Order> orders = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findAllByCustomerIdOrderByOrderCreatedAtDesc(anyLong(), any(Pageable.class))).thenReturn(orders);
        when(bookAdaptor.getAllByBookIds(anyList())).thenReturn(List.of(readBookInfoResponse));

        // when
        Page<ReadMyOrderHistoryResponse> responses = myPageOrderService.getMyOrdersByPaging(pageable, 1L);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(1);

        ReadMyOrderHistoryResponse response = responses.getContent().get(0);
        assertThat(response.bookIds()).containsExactly(1L);
        assertThat(response.quantities()).containsExactly(2);
        assertThat(response.bookNames()).containsExactly("Test Book");
    }
}
