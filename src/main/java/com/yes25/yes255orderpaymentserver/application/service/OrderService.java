package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    void save(PreOrder preOrder);

    Page<ReadUserOrderAllResponse> findByUserId(Long userId,
        Pageable pageable);

    ReadUserOrderResponse findByOrderIdAndUserId(String orderId, Long userId);
}
