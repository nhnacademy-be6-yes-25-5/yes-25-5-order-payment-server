package com.yes25.yes255orderpaymentserver.application.service.impl;


import com.yes25.yes255orderpaymentserver.application.dto.request.ReadBookNameResponse;
import com.yes25.yes255orderpaymentserver.application.service.MyPageOrderService;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadMyOrderHistoryResponse;
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
public class MyPageOrderServiceImpl implements MyPageOrderService {

    private final OrderRepository orderRepository;
    private final OrderBookRepository orderBookRepository;
    private final BookAdaptor bookAdaptor;

    @Transactional(readOnly = true)
    @Override
    public Page<ReadMyOrderHistoryResponse> getMyOrdersByPaging(Pageable pageable, Long userId) {
        Page<Order> orders = orderRepository.findAllByCustomerIdOrderByOrderCreatedAtDesc(userId, pageable);

        List<ReadMyOrderHistoryResponse> responses = orders.stream().map(order -> {
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

            return ReadMyOrderHistoryResponse.of(order, bookIds, quantities, bookNames);
        }).toList();

        return new PageImpl<>(responses, pageable, orders.getTotalElements());
    }
}
