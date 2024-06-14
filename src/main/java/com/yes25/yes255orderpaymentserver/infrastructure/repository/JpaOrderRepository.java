package com.yes25.yes255orderpaymentserver.infrastructure.repository;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {

}
