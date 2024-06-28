package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.ReadBookNameResponse;
import com.yes25.yes255orderpaymentserver.application.service.AdminOrderService;
import com.yes25.yes255orderpaymentserver.common.exception.OrderNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderStatusNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderStatusRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderBookRepository orderBookRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final BookAdaptor bookAdaptor;

    @Transactional(readOnly = true)
    @Override
    public Page<ReadAllOrderResponse> getAllOrdersByPaging(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByOrderByOrderCreatedAtDesc(pageable);
        List<ReadAllOrderResponse> responses = orders.stream().map(order -> {
            List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
            List<Long> bookIds = orderBooks.stream()
                .map(OrderBook::getBookId)
                .toList();

            List<Integer> quantities = orderBooks.stream()
                .map(OrderBook::getOrderBookQuantity)
                .toList();

            List<ReadBookNameResponse> bookNameResponses = bookAdaptor.getAllByBookIds(bookIds);
            List<String> bookNames = bookNameResponses.stream()
                .map(ReadBookNameResponse::bookName)
                .toList();

            return ReadAllOrderResponse.of(order, bookIds, quantities, bookNames);
        }).toList();

        return new PageImpl<>(responses, pageable, orders.getTotalElements());
    }

    @Override
    public void updateOrderStatusByOrderId(String orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(
                ErrorStatus.toErrorStatus("주문을 찾을 수 없습니다. 주문 ID : " + orderId, 404, LocalDateTime.now())
            ));

        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(request.orderStatusType().name())
            .orElseThrow(() -> new OrderStatusNotFoundException(request.orderStatusType().name()));

        order.updateOrderStatus(orderStatus);
    }
}
