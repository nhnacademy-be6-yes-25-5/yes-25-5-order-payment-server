package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadMyOrderHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MyPageOrderService {

    Page<ReadMyOrderHistoryResponse> getMyOrdersByPaging(Pageable pageable, Long userId);
}
