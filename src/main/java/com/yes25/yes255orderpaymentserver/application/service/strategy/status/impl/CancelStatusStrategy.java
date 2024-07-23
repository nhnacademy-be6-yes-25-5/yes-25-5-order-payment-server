package com.yes25.yes255orderpaymentserver.application.service.strategy.status.impl;

import com.yes25.yes255orderpaymentserver.application.service.context.PaymentContext;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.application.service.strategy.status.OrderStatusStrategy;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderCoupon;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Component("cancel")
public class CancelStatusStrategy implements OrderStatusStrategy {

    private final PaymentContext paymentContext;
    private final OrderBookRepository orderBookRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final MessageProducer messageProducer;

    @Override
    public void updateOrderStatus(Order order, UpdateOrderRequest request) {
        if (!order.isWaitEqualTo()) {
            throw new AccessDeniedException("결제 취소는 대기중일때만 가능합니다. 주문 ID : " + order.getOrderId());
        }

        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(request.orderStatusType().name())
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 주문 상태를 찾을 수 없습니다. 요청 : " + request.orderStatusType().name(),
                    404,
                    LocalDateTime.now())));

        log.info("사용자 요청으로 인해 결제 취소를 진행합니다.");

        if (order.getPayment().getPaymentAmount().compareTo(BigDecimal.ZERO) != 0) {
            paymentContext.cancelPayment(order.getPayment().getPaymentKey(), "사용자 요청",
                order.getPayment().getPaymentAmount().intValue(),
                order.getOrderId(), request.paymentProvider().name().toLowerCase());
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

        order.updateOrderStatusAndUpdatedAt(orderStatus, LocalDateTime.now());

        messageProducer.sendOrderCancelMessageByUser(bookIds, quantities,
            couponIds, order.getPoints(), order.getPurePrice());
    }
}
