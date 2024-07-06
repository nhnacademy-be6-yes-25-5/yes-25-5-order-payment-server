package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.presentation.dto.request.CancelOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderStatusRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CancelOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllUserOrderCancelStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminOrderService {

    Page<ReadAllOrderResponse> getAllOrdersByPaging(Pageable pageable, String role);

    void updateOrderStatusByOrderId(String orderId, UpdateOrderStatusRequest request);

    Page<ReadAllUserOrderCancelStatusResponse> getAllCancelOrdersByPaging(Pageable pageable);

    CancelOrderResponse cancelOrderByOrderId(String orderId, CancelOrderRequest request);
}
