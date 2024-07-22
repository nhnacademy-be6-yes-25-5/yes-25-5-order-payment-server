package com.yes25.yes255orderpaymentserver.application.service.context;

import com.yes25.yes255orderpaymentserver.application.service.strategy.status.OrderStatusStrategy;
import com.yes25.yes255orderpaymentserver.application.service.strategy.status.OrderStatusStrategyProvider;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderStatusContext {

    private final OrderRepository orderRepository;
    private final OrderStatusStrategyProvider orderStatusStrategyProvider;

    public UpdateOrderResponse updateOrderStatusByOrderId(String orderId,
        UpdateOrderRequest request, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 주문을 찾을 수 없습니다. 주문 ID : " + orderId, 404, LocalDateTime.now())));

        if (!order.isCustomerIdEqualTo(userId)) {
            throw new AccessDeniedException("주문 내역의 정보와 사용자가 일치하지 않습니다. 사용자 ID : " + userId);
        }

        OrderStatusStrategy orderStatusStrategy = orderStatusStrategyProvider.getStrategy(request.orderStatusType().name().toLowerCase());
        orderStatusStrategy.updateOrderStatus(order, request);

        return UpdateOrderResponse.from("주문 상태가 성공적으로 변경되었습니다.");
    }
}
