package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.ReadBookInfoResponse;
import com.yes25.yes255orderpaymentserver.application.service.AdminOrderService;
import com.yes25.yes255orderpaymentserver.application.service.PaymentProcessor;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderStatusNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.RefundStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Delivery;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;
import com.yes25.yes255orderpaymentserver.persistance.repository.DeliveryRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundStatusRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CancelOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderStatusRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CancelOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadAllUserOrderCancelStatusResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DeliveryRepository deliveryRepository;
    private final RefundRepository refundRepository;
    private final RefundStatusRepository refundStatusRepository;

    private final PaymentProcessor paymentProcessor;

    private final BookAdaptor bookAdaptor;

    @Transactional(readOnly = true)
    @Override
    public Page<ReadAllOrderResponse> getAllOrdersByPaging(Pageable pageable, String role) {
        Page<Order> orders;
        if (Objects.isNull(role) || role.isEmpty()) {
            orders = orderRepository.findAllByOrderByOrderCreatedAtDesc(pageable);
        } else {
            orders = orderRepository.findAllByUserRoleOrderByOrderCreatedAtDesc(role, pageable);
        }

        List<ReadAllOrderResponse> responses = orders.stream().map(order -> {
            List<OrderBook> orderBooks = order.getOrderBooks();
            List<Long> bookIds = orderBooks.stream()
                .map(OrderBook::getBookId)
                .toList();

            List<Integer> quantities = orderBooks.stream()
                .map(OrderBook::getOrderBookQuantity)
                .toList();

            List<ReadBookInfoResponse> bookNameResponses = bookAdaptor.getAllByBookIds(bookIds);
            List<String> bookNames = bookNameResponses.stream()
                .map(ReadBookInfoResponse::bookName)
                .toList();

            return ReadAllOrderResponse.of(order, bookIds, quantities, bookNames);
        }).toList();

        return new PageImpl<>(responses, pageable, orders.getTotalElements());
    }

    @Override
    public void updateOrderStatusByOrderId(String orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(
                request.orderStatusType().name())
            .orElseThrow(() -> new OrderStatusNotFoundException(request.orderStatusType().name()));

        order.updateOrderStatusAndUpdatedAtAndDeliveryStartedAt(orderStatus, LocalDateTime.now());

        Delivery delivery = request.toEntity(order);
        deliveryRepository.save(delivery);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ReadAllUserOrderCancelStatusResponse> getAllCancelOrdersByPaging(
        Pageable pageable) {
        Page<Refund> refunds = refundRepository.findAllByRefundStatus_RefundStatusName(CancelStatus.WAIT.name(), pageable);

        List<ReadAllUserOrderCancelStatusResponse> responses = refunds.stream().map(refund -> {
            List<OrderBook> orderBooks = refund.getOrder().getOrderBooks();
            List<Long> bookIds = orderBooks.stream()
                .map(OrderBook::getBookId)
                .toList();

            List<ReadBookInfoResponse> bookNameResponses = bookAdaptor.getAllByBookIds(bookIds);
            List<String> bookNames = bookNameResponses.stream()
                .map(ReadBookInfoResponse::bookName)
                .toList();

            return ReadAllUserOrderCancelStatusResponse.of(refund, bookIds, bookNames);
        }).toList();

        return new PageImpl<>(responses, pageable, refunds.getTotalElements());
    }

    @Override
    public CancelOrderResponse cancelOrderByOrderId(String orderId, CancelOrderRequest request) {
        Refund refund = refundRepository.findByOrder_OrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 환불 정보를 찾을 수 없습니다. 주문 ID : " + orderId, 404,
                    LocalDateTime.now())));

        if (request.status().equals(CancelStatus.ACCESS)) {
            paymentProcessor.cancelPayment(refund.getOrder().getPayment().getPaymentKey(), "고객 요청",
                refund.getOrder().getPayment().getPaymentAmount().intValue(), refund.getOrder().getOrderId());

            log.info("주문이 관리자에 의해 성공적으로 환불되었습니다.");
        } else {
            log.info("주문이 관리자에 의해 거부되었습니다.");
        }

        RefundStatus refundStatus = refundStatusRepository.findByRefundStatusName(request.status().name())
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 환불 상태를 찾을 수 없습니다.", 404, LocalDateTime.now())
            ));

        refund.updateRefundStatusAndUpdatedAt(refundStatus, LocalDateTime.now());

        return CancelOrderResponse.from(request.status());
    }
}
