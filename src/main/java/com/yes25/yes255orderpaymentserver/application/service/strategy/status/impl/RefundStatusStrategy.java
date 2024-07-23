package com.yes25.yes255orderpaymentserver.application.service.strategy.status.impl;

import com.yes25.yes255orderpaymentserver.application.service.strategy.status.OrderStatusStrategy;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundStatusRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("refund")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundStatusStrategy implements OrderStatusStrategy {

    private final RefundRepository refundRepository;
    private final RefundStatusRepository refundStatusRepository;

    @Override
    public void updateOrderStatus(Order order, UpdateOrderRequest request) {
        if (!order.isReturnEqualTo()) {
            throw new AccessDeniedException("환불은 반품이 완료되었을때만 가능합니다. 주문 ID : " + order.getOrderId());
        }

        RefundStatus refundStatus = refundStatusRepository.findByRefundStatusName(
                CancelStatus.WAIT.name())
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 환불 상태를 찾을 수 없습니다.", 404, LocalDateTime.now())
            ));

        Refund refund = Refund.toEntity(order, refundStatus);
        refundRepository.save(refund);
    }
}
