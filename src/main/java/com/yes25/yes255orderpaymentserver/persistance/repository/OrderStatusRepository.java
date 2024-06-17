package com.yes25.yes255orderpaymentserver.persistance.repository;

import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    Optional<OrderStatus> findByOrderStatusName(String name);

}
