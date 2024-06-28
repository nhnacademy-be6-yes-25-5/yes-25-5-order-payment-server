package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminOrderService {

    Page<ReadAllOrderResponse> getAllOrdersByPaging(Pageable pageable);
}
