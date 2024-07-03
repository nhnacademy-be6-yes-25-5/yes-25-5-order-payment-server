package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateRefundRequest;
import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.ReadPurePriceResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.application.service.PaymentProcessor;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderStatusNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.PaymentNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Delivery;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.persistance.domain.ShippingPolicy;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.repository.DeliveryRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.ShippingPolicyRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.TakeoutRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDeliveryResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDetailResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;
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
    private final BookAdaptor bookAdaptor;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessor paymentProcessor;
    private final ShippingPolicyRepository shippingPolicyRepository;
    private final UserAdaptor userAdaptor;

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

        Payment payment = paymentRepository.findByPreOrderId(savedOrder.getOrderId())
            .orElseThrow(() -> new PaymentNotFoundException(savedOrder.getOrderId()));
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
                ErrorStatus.toErrorStatus("해당하는 엔티티를 찾을 수 없습니다. : " + orderId, 404,
                    LocalDateTime.now())
            ));

        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);

        return ReadUserOrderResponse.fromEntities(order, orderBooks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReadPaymentOrderResponse> findAllOrderByOrderId(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
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
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        return ReadOrderStatusResponse.fromEntity(order);
    }

    @Override
    public void updateOrderStatusToDone() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findByOrderStatusOrderStatusNameAndDeliveryStartedAtBefore(
            OrderStatusType.DELIVERING.name(), now);
        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(
                OrderStatusType.DONE.name())
            .orElseThrow(() -> new OrderStatusNotFoundException(OrderStatusType.DONE.name()));

        for (Order order : orders) {
            order.updateOrderStatusAndUpdatedAt(orderStatus, now);
            Delivery delivery = Delivery.toEntity(order);
            deliveryRepository.save(delivery);
        }
    }

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
            handleCancelRequest(order, orderId);
        }

        if (request.orderStatusType().name().equals(OrderStatusType.REFUND.name())) {
            handleReturnRequest(order, orderId);
        }

        order.updateOrderStatusAndUpdatedAt(orderStatus, LocalDateTime.now());

        return UpdateOrderResponse.from("주문 상태가 성공적으로 변경되었습니다.");
    }

    private void handleReturnRequest(Order order, String orderId) {
        if (order.getDeliveryStartedAt() != null) {
            ShippingPolicy returnPolicy = shippingPolicyRepository.findByShippingPolicyIsRefundPolicyTrue()
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

    private void handleCancelRequest(Order order, String orderId) {
        if (!order.isWaitEqualTo()) {
            throw new AccessDeniedException("결제 취소는 대기중일때만 가능합니다. 주문 ID : " + orderId);
        }

        log.info("사용자 요청으로 인해 결제를 취소합니다.");
        paymentProcessor.cancelPayment(order.getPayment().getPaymentKey(), "사용자 요청",
            order.getPayment().getPaymentAmount().intValue(),
            orderId);
    }

    @Override
    public ReadOrderDeliveryResponse getByOrderIdAndUserId(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
        List<ReadBookResponse> responses = getBookResponse(orderBooks);

        List<Delivery> deliveries = deliveryRepository.findAllByOrderOrderByTimestampDesc(order);

        return ReadOrderDeliveryResponse.of(order, responses, deliveries);
    }

    @Override
    public List<ReadPurePriceResponse> getPurePriceByDate(LocalDate now) {
        LocalDate threeMonthsAgo = now.minusMonths(3);
        List<Order> orders = orderRepository.findAllByOrderCreatedAtBetween(threeMonthsAgo.atStartOfDay(), now.atTime(LocalTime.MAX));
        List<Long> orderUserIds = orders.stream()
            .map(Order::getCustomerId)
            .distinct()
            .toList();

        List<ReadPurePriceResponse> purePriceResponses = new ArrayList<>();

        for (Long orderUserId : orderUserIds) {
            List<Order> allOrders = orderRepository.findAllByCustomerId(orderUserId);
            List<Order> cancelOrders = orderRepository.findAllByCustomerIdAndOrderStatusOrderStatusName(orderUserId, OrderStatusType.CANCEL.name());

            BigDecimal totalPurPriceWithoutCancel = allOrders.stream()
                .map(Order::getPurePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCancelAmount = cancelOrders.stream()
                .map(Order::getOrderTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal purePriceWithCancel = totalPurPriceWithoutCancel.subtract(totalCancelAmount);

            ReadPurePriceResponse readPurePriceResponse = ReadPurePriceResponse.from(purePriceWithCancel, orderUserId);
            purePriceResponses.add(readPurePriceResponse);
        }

        return purePriceResponses;
    }

    @Override
    public ReadOrderDetailResponse getOrderByOrderId(String orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.isCustomerIdEqualTo(userId)) {
            throw new AccessDeniedException("주문 내역의 정보와 사용자가 일치하지 않습니다. 사용자 ID : " + userId);
        }
        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
        List<ReadBookResponse> responses = getBookResponse(orderBooks);

        return ReadOrderDetailResponse.of(order, responses, orderBooks);
    }

    @Override
    public ReadOrderDetailResponse getOrderByOrderIdAndEmail(String orderId, String email) {
        Order order = orderRepository.findByOrderIdAndOrderUserEmail(orderId, email)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
        List<ReadBookResponse> responses = getBookResponse(orderBooks);

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
