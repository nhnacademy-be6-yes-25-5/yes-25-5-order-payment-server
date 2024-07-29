package com.yes25.yes255orderpaymentserver.application.service.strategy.status.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateRefundRequest;
import com.yes25.yes255orderpaymentserver.application.service.strategy.status.OrderStatusStrategy;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.ShippingPolicyRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Component("return")
public class ReturnStatusStrategy implements OrderStatusStrategy {

    private final ShippingPolicyRepository shippingPolicyRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final UserAdaptor userAdaptor;

    @Override
    public void updateOrderStatus(Order order, UpdateOrderRequest request) {
        if (order.getDeliveryStartedAt() != null) {
            ShippingPolicy returnPolicy = shippingPolicyRepository.findByShippingPolicyIsReturnPolicyTrue()
                .orElseThrow(() -> new EntityNotFoundException(
                    ErrorStatus.toErrorStatus("반품 배송비 정책을 찾을 수 없습니다.", 404, LocalDateTime.now())));

            OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(request.orderStatusType().name())
                .orElseThrow(() -> new EntityNotFoundException(
                    ErrorStatus.toErrorStatus("해당하는 주문 상태를 찾을 수 없습니다.", 404, LocalDateTime.now())));

            BigDecimal returnAmount = calculateRefundAmount(order, returnPolicy);
            UpdateRefundRequest refundRequest = UpdateRefundRequest.from(returnAmount);

            order.updateOrderStatusAndUpdatedAt(orderStatus, LocalDateTime.now());

            userAdaptor.updatePointByRefund(refundRequest);

        } else {
            throw new AccessDeniedException("배송이 시작되지 않은 주문은 반품할 수 없습니다. 주문 ID : " + order.getOrderId());
        }
    }

    private BigDecimal calculateRefundAmount(Order order, ShippingPolicy refundPolicy) {
        LocalDateTime deliveryDate = order.getDeliveryStartedAt();
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(deliveryDate.plusDays(10))) {
            log.info("출고일로부터 10일 이내 반품입니다. 반품 택배비 차감 후 반품 처리됩니다.");
        } else if (now.isBefore(deliveryDate.plusDays(30))) {
            log.info("출고일로부터 30일 이내 반품입니다. 반품 택배비 차감 후 반품 처리됩니다.");
        } else {
            throw new AccessDeniedException("반품 가능 기간이 지났습니다. 주문 ID : " + order.getOrderId());
        }

        return order.getPayment().getPaymentDetail().getPaymentAmount()
            .subtract(refundPolicy.getShippingPolicyFee());
    }
}
