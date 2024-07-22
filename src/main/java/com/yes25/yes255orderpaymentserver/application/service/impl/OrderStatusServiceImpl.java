package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateRefundRequest;
import com.yes25.yes255orderpaymentserver.application.service.OrderStatusService;
import com.yes25.yes255orderpaymentserver.application.service.context.PaymentContext;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderStatusNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderCoupon;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.ShippingPolicyRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final RefundRepository refundRepository;
    private final RefundStatusRepository refundStatusRepository;
    private final ShippingPolicyRepository shippingPolicyRepository;
    private final OrderBookRepository orderBookRepository;

    private final PaymentContext paymentContext;
    private final MessageProducer messageProducer;

    private final UserAdaptor userAdaptor;

    @Override
    public UpdateOrderResponse updateOrderStatusByOrderId(String orderId,
        UpdateOrderRequest request, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(
                request.orderStatusType().name())
            .orElseThrow(() -> new OrderStatusNotFoundException(request.orderStatusType().name()));

        if (!order.isCustomerIdEqualTo(userId)) {
            throw new AccessDeniedException("주문 내역의 정보와 사용자가 일치하지 않습니다. 사용자 ID : " + userId);
        }

        if (request.orderStatusType().name().equals(OrderStatusType.CANCEL.name())) {
            handleCancelRequest(order, orderId, request.paymentProvider().name().toLowerCase());
            order.updateOrderStatusAndUpdatedAt(orderStatus, LocalDateTime.now());
        }

        if (request.orderStatusType().name().equals(OrderStatusType.RETURN.name())) {
            handleReturnRequest(order, orderId);
            order.updateOrderStatusAndUpdatedAt(orderStatus, LocalDateTime.now());
        }

        if (request.orderStatusType().name().equals(OrderStatusType.REFUND.name())) {
            handleRefundRequest(order, orderId);
        }

        return UpdateOrderResponse.from("주문 상태가 성공적으로 변경되었습니다.");
    }

    private void handleRefundRequest(Order order, String orderId) {
        if (!order.isReturnEqualTo()) {
            throw new AccessDeniedException("환불은 반품이 완료되었을때만 가능합니다. 주문 ID : " + orderId);
        }

        RefundStatus refundStatus = refundStatusRepository.findByRefundStatusName(CancelStatus.WAIT.name())
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 환불 상태를 찾을 수 없습니다.", 404, LocalDateTime.now())
            ));

        Refund refund = Refund.toEntity(order, refundStatus);
        refundRepository.save(refund);
    }

    private void handleReturnRequest(Order order, String orderId) {
        if (order.getDeliveryStartedAt() != null) {
            ShippingPolicy returnPolicy = shippingPolicyRepository.findByShippingPolicyIsReturnPolicyTrue()
                .orElseThrow(() -> new EntityNotFoundException(
                    ErrorStatus.toErrorStatus("반품 배송비 정책을 찾을 수 없습니다.", 404, LocalDateTime.now())));

            BigDecimal returnAmount = calculateRefundAmount(order, returnPolicy);
            UpdateRefundRequest refundRequest = UpdateRefundRequest.from(returnAmount);

            userAdaptor.updatePointByRefund(refundRequest);

        } else {
            throw new AccessDeniedException("배송이 시작되지 않은 주문은 반품할 수 없습니다. 주문 ID : " + orderId);
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

        return order.getPayment().getPaymentAmount()
            .subtract(refundPolicy.getShippingPolicyFee());
    }

    private void handleCancelRequest(Order order, String orderId, String paymentProvider) {
        if (!order.isWaitEqualTo()) {
            throw new AccessDeniedException("결제 취소는 대기중일때만 가능합니다. 주문 ID : " + orderId);
        }

        log.info("사용자 요청으로 인해 결제 취소를 진행합니다.");

        if (order.getPayment().getPaymentAmount().compareTo(BigDecimal.ZERO) != 0) {
            paymentContext.cancelPayment(order.getPayment().getPaymentKey(), "사용자 요청",
                order.getPayment().getPaymentAmount().intValue(),
                orderId, paymentProvider);
        }

        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
        List<Long> bookIds = orderBooks.stream()
            .map(OrderBook::getBookId)
            .toList();

        List<Integer> quantities = orderBooks.stream()
            .map(OrderBook::getOrderBookQuantity)
            .toList();

        List<Long> couponIds = order.getOrderCoupons().stream()
            .map(OrderCoupon::getUserCouponId)
            .toList();

        messageProducer.sendOrderCancelMessageByUser(bookIds, quantities,
            couponIds, order.getPoints(), order.getPurePrice());
    }
}