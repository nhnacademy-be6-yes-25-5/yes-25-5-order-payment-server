package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.ReadPurePriceResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Delivery;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderCoupon;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.persistance.domain.Refund;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.repository.DeliveryRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderCouponRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.RefundRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.TakeoutRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDeliveryResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDetailResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final TakeoutRepository takeoutRepository;
    private final OrderBookRepository orderBookRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final OrderCouponRepository orderCouponRepository;

    private final BookAdaptor bookAdaptor;

    @Override
    public void createOrder(PreOrder preOrder, BigDecimal purePrice,
        SuccessPaymentResponse response) {
        log.info("결제가 완료되어 주문을 확정하는 중입니다. 주문 ID : {}", preOrder.getPreOrderId());
        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(
                OrderStatusType.WAIT.name())
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("주문 상태를 찾을 수 없습니다.", 404, LocalDateTime.now())));

        Takeout takeout = takeoutRepository.findByTakeoutName(preOrder.getTakeoutType().name())
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("포장 정보를 찾을 수 없습니다.", 404, LocalDateTime.now())));

        Order order = preOrder.toEntity(orderStatus, takeout, purePrice);
        Order savedOrder = orderRepository.save(order);

        List<OrderCoupon> orderCoupons = new ArrayList<>();
        for (Long couponId : preOrder.getCouponIds()) {
            OrderCoupon orderCoupon = preOrder.toOrderCoupon(savedOrder, couponId);
            orderCoupons.add(orderCoupon);
        }

        orderCouponRepository.saveAll(orderCoupons);

        Payment payment = paymentRepository.findByPreOrderId(savedOrder.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorStatus.toErrorStatus(
                "해당 주문에 대한 결제 정보를 찾을 수 없습니다. 주문 ID : " + savedOrder.getOrderId(), 404, LocalDateTime.now())));
        payment.addOrder(savedOrder);

        List<OrderBook> orderBooks = new ArrayList<>();
        for (int i = 0; i < preOrder.getBookIds().size(); i++) {
            OrderBook orderBook = preOrder.toOrderProduct(savedOrder, i);
            orderBooks.add(orderBook);
        }

        orderBookRepository.saveAll(orderBooks);
        log.info("주문이 확정되었습니다. 주문 ID: {}", preOrder.getPreOrderId());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ReadUserOrderAllResponse> findByUserId(Long userId,
        Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByCustomerIdOrderByOrderCreatedAtDesc(userId,
            pageable);

        List<ReadUserOrderAllResponse> responses = orders.stream()
            .map(order -> {
                List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
                return ReadUserOrderAllResponse.fromEntity(order, orderBooks);
            })
            .toList();

        return new PageImpl<>(responses, pageable, orders.getTotalElements());
    }

    @Transactional(readOnly = true)
    @Override
    public ReadUserOrderResponse findByOrderIdAndUserId(String orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 주문을 찾을 수 없습니다. : " + orderId, 404,
                    LocalDateTime.now())
            ));

        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);

        return ReadUserOrderResponse.fromEntities(order, orderBooks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReadPaymentOrderResponse> findAllOrderByOrderId(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 주문을 찾을 수 없습니다. 주문 ID : " + orderId, 404, LocalDateTime.now())));
        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
        List<ReadBookResponse> responses = getBookResponse(orderBooks);

        return responses.stream()
            .map(ReadPaymentOrderResponse::fromDto)
            .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ReadOrderStatusResponse findOrderStatusByOrderId(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 주문을 찾을 수 없습니다. 주문 ID : " + orderId, 404, LocalDateTime.now())
            ));

        return ReadOrderStatusResponse.fromEntity(order);
    }

    @Override
    public void updateOrderStatusToDone() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findByOrderStatusOrderStatusNameAndDeliveryStartedAtBefore(
            OrderStatusType.DELIVERING.name(), now);
        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(
                OrderStatusType.DONE.name())
            .orElseThrow(() -> new EntityNotFoundException(ErrorStatus.toErrorStatus(
                "해당하는 주문 상태를 찾을 수 없습니다. 요청 정보 : " + OrderStatusType.DONE.name(), 404, LocalDateTime.now())));

        for (Order order : orders) {
            order.updateOrderStatusAndUpdatedAt(orderStatus, now);
            Delivery delivery = Delivery.toEntity(order);
            deliveryRepository.save(delivery);
        }
    }

    @Override
    public ReadOrderDeliveryResponse getByOrderIdAndUserId(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 주문을 찾을 수 없습니다. 주문 ID : " + orderId, 404, LocalDateTime.now())
            ));
        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
        List<ReadBookResponse> responses = getBookResponse(orderBooks);

        List<Delivery> deliveries = deliveryRepository.findAllByOrderOrderByTimestampDesc(order);

        return ReadOrderDeliveryResponse.of(order, responses, deliveries);
    }

    /**
     * 3개월치 순수 주문금액을 계산하는 메소드입니다. orders 테이블 내 순수 주문 금액은 취소 금액을 제외한 순수 금액입니다.
     * 주문한 이력이 있는 회원의 취소를 제외한 순수 주문 금액을 더하고, 이를 취소한 금액만큼 뺍니다.
     * @param now 현재 날찌
     * */
    @Override
    public List<ReadPurePriceResponse> getPurePriceByDate(LocalDate now) {
        LocalDate threeMonthsAgo = now.minusMonths(3);
        List<Order> orders = orderRepository.findAllByOrderCreatedAtBetween(
            threeMonthsAgo.atStartOfDay(), now.atTime(LocalTime.MAX));
        List<Long> orderUserIds = orders.stream()
            .map(Order::getCustomerId)
            .distinct()
            .toList();

        List<ReadPurePriceResponse> purePriceResponses = new ArrayList<>();

        for (Long orderUserId : orderUserIds) {
            List<Order> allOrders = orderRepository.findAllByCustomerId(orderUserId);
            List<Order> cancelOrders = orderRepository.findAllByCustomerIdAndOrderStatusOrderStatusName(
                orderUserId, OrderStatusType.CANCEL.name());

            BigDecimal totalPurPriceWithoutCancel = allOrders.stream()
                .map(Order::getPurePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCancelAmount = cancelOrders.stream()
                .map(Order::getOrderTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal purePriceWithCancel = totalPurPriceWithoutCancel.subtract(totalCancelAmount);

            ReadPurePriceResponse readPurePriceResponse = ReadPurePriceResponse.from(
                purePriceWithCancel, orderUserId);
            purePriceResponses.add(readPurePriceResponse);
        }

        return purePriceResponses;
    }

    @Override
    public ReadOrderDetailResponse getOrderByOrderId(String orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 주문을 찾을 수 없습니다. 주문 ID : " + orderId, 404, LocalDateTime.now())
            ));

        if (!order.isCustomerIdEqualTo(userId)) {
            throw new AccessDeniedException("주문 내역의 정보와 사용자가 일치하지 않습니다. 사용자 ID : " + userId);
        }

        return getOrderDetailResponse(order);
    }

    @Override
    public ReadOrderDetailResponse getOrderByOrderIdAndEmailForNoneMember(String orderId, String email) {
        Order order = orderRepository.findByOrderIdAndOrderUserEmail(orderId, email)
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("해당하는 주문을 찾을 수 없습니다. 주문 ID : " + orderId, 404, LocalDateTime.now())
            ));

        return getOrderDetailResponse(order);
    }

    @Override
    public Boolean existOrderHistoryByUserIdAndBookId(Long userId, Long bookId) {
        List<Order> orders = orderRepository.findAllByCustomerId(userId);
        for (Order order : orders) {
            for (OrderBook orderBook : order.getOrderBooks()) {
                if (orderBook.getBookId().equals(bookId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private ReadOrderDetailResponse getOrderDetailResponse(Order order) {
        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
        List<ReadBookResponse> responses = getBookResponse(orderBooks);

        if (refundRepository.existsByOrder(order)) {
            Refund refund = refundRepository.findByOrder_OrderId(order.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException(
                    ErrorStatus.toErrorStatus("해당하는 환불 정보를 찾을 수 없습니다. 주문 ID : " + order.getOrderId(), 404, LocalDateTime.now())
                ));
            return ReadOrderDetailResponse.of(order, responses, orderBooks, refund);
        }

        return ReadOrderDetailResponse.of(order, responses, orderBooks);
    }

    private List<ReadBookResponse> getBookResponse(List<OrderBook> orderBooks) {
        List<ReadBookResponse> responses = new ArrayList<>();
        for (OrderBook orderBook : orderBooks) {
            ReadBookResponse response = bookAdaptor.findBookById(orderBook.getBookId());
            responses.add(response);
        }

        return responses;
    }
}
