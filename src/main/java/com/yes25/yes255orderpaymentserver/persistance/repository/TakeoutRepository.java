package com.yes25.yes255orderpaymentserver.persistance.repository;

import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TakeoutRepository extends JpaRepository<Takeout, Long> {

    Optional<Takeout> findByTakeoutName(String name);
}
