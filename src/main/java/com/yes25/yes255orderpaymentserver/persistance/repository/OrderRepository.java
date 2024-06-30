package com.yes25.yes255orderpaymentserver.persistance.repository;

import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {

    Page<Order> findAllByCustomerIdOrderByOrderCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findAllByOrderByOrderCreatedAtDesc(Pageable pageable);

    List<Order> findByOrderStatusOrderStatusNameAndUpdatedAtBefore(String doneName, LocalDateTime now);

    Page<Order> findAllByUserRoleOrderByOrderCreatedAtDesc(String role, Pageable pageable);
}
