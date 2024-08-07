package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadPurePriceResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDeliveryResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDetailResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    void createOrder(PreOrder preOrder, BigDecimal purePrice, SuccessPaymentResponse successPaymentResponse);

    Page<ReadUserOrderAllResponse> findByUserId(Long userId,
        Pageable pageable);

    ReadUserOrderResponse findByOrderIdAndUserId(String orderId, Long userId);

    List<ReadPaymentOrderResponse> findAllOrderByOrderId(String orderId);

    ReadOrderStatusResponse findOrderStatusByOrderId(String orderId);

    void updateOrderStatusToDone();

    ReadOrderDeliveryResponse getByOrderIdAndUserId(String orderId);

    List<ReadPurePriceResponse> getPurePriceByDate(LocalDate now);

    ReadOrderDetailResponse getOrderByOrderId(String orderId, Long userId);

    ReadOrderDetailResponse getOrderByOrderIdAndEmailForNoneMember(String orderId, String email);

    Boolean existOrderHistoryByUserIdAndBookId(Long userId, Long bookId);
}
