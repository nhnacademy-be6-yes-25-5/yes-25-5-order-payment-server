package com.yes25.yes255orderpaymentserver.persistance.repository;

import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundStatusRepository extends JpaRepository<RefundStatus, Long> {

    Optional<RefundStatus> findByRefundStatusName(String name);
}
