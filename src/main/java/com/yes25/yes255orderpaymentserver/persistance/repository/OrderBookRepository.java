package com.yes25.yes255orderpaymentserver.persistance.repository;

import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {

}
