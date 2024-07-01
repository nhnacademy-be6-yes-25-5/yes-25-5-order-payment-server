package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.ReadPurePriceResponse;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.common.exception.AccessDeniedException;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.OrderStatusNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Delivery;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.repository.DeliveryRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderBookRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.TakeoutRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.UpdateOrderRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderDetailResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadOrderStatusResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderAllResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadUserOrderResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.UpdateOrderResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        List<ReadBookResponse> responses = getBookResponse(order);

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
        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(request.orderStatusType().name())
            .orElseThrow(() -> new OrderStatusNotFoundException(request.orderStatusType().name()));

        if (!order.isCustomerIdEqualTo(userId)) {
            throw new AccessDeniedException("주문 내역의 정보와 사용자가 일치하지 않습니다.");
        }
        order.updateOrderStatusAndUpdatedAt(orderStatus, LocalDateTime.now());

        return UpdateOrderResponse.from("주문 상태가 성공적으로 변경되었습니다.");
    }

    @Override
    public ReadOrderDetailResponse getByOrderIdAndUserId(String orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        List<ReadBookResponse> responses = getBookResponse(order);

        List<Delivery> deliveries = deliveryRepository.findAllByOrderOrderByTimestampDesc(order);

        return ReadOrderDetailResponse.of(order, responses, deliveries);
    }

    // todo. 3달치 모든 회원 순수금액 계산
    @Override
    public ReadPurePriceResponse getPurePriceByDate(LocalDate now) {
        return null;
    }

    private List<ReadBookResponse> getBookResponse(Order order) {
        List<OrderBook> orderBooks = orderBookRepository.findByOrder(order);
        List<ReadBookResponse> responses = new ArrayList<>();
        for (OrderBook orderBook : orderBooks) {
            ReadBookResponse response = bookAdaptor.findBookById(orderBook.getBookId());
            responses.add(response);
        }

        return responses;
    }
}
