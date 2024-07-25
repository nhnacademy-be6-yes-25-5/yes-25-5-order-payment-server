package com.yes25.yes255orderpaymentserver.application.service.strategy.payment;

import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.TossAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Order;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderCoupon;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PaymentDetail;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.OrderStatusType;
import com.yes25.yes255orderpaymentserver.persistance.repository.OrderStatusRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentDetailRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentRetryService {

    private final MessageProducer messageProducer;
    private final TossAdaptor tossAdaptor;

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final OrderStatusRepository orderStatusRepository;

    public void retryPaymentConfirm(CreatePaymentRequest request, String authorizations,
        JSONObject obj, int attempt, Long paymentId, int maxAttempts, int retryDelayMs) {

        while (attempt < maxAttempts) {
            try {
                Thread.sleep(retryDelayMs);

                JSONObject response = tossAdaptor.confirmPayment(authorizations, obj.toString());

                Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorStatus.toErrorStatus(
                        "결제 정보를 찾을 수 없습니다. 결제 ID : " + paymentId, 404, LocalDateTime.now())));

                PaymentDetail paymentDetail = PaymentDetail.from(response, payment);
                paymentDetailRepository.save(paymentDetail);

                log.info("결제가 성공적으로 이루어졌습니다. {}", paymentId);

                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ignore) {
                attempt++;
            }
        }

        log.error("결제 재시도 최종 실패 : {}", request);
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorStatus.toErrorStatus(
                "결제 정보를 찾을 수 없습니다. 결제 ID : " + paymentId, 404, LocalDateTime.now())));

        sendPaymentRetryFailureMessage(payment);
        updateOrderStatusToPaymentConfirmDenied(payment);
    }

    private void updateOrderStatusToPaymentConfirmDenied(Payment payment) {
        Order order = payment.getOrder();
        OrderStatus orderStatus = orderStatusRepository.findByOrderStatusName(OrderStatusType.PAYMENT_CONFIRM_DENIED.name())
            .orElseThrow(() -> new EntityNotFoundException(
                ErrorStatus.toErrorStatus("주문 상태를 찾을 수 없습니다.", 404, LocalDateTime.now())));

        order.updateOrderStatusAndUpdatedAt(orderStatus, LocalDateTime.now());
    }

    private void sendPaymentRetryFailureMessage(Payment payment) {
        List<Long> bookIds = payment.getOrder().getOrderBooks().stream()
            .map(OrderBook::getBookId)
            .toList();

        List<Integer> quantities = payment.getOrder().getOrderBooks().stream()
            .map(OrderBook::getOrderBookQuantity)
            .toList();

        List<Long> couponIds = payment.getOrder().getOrderCoupons().stream()
            .map(OrderCoupon::getUserCouponId)
            .toList();

        BigDecimal point = payment.getOrder().getPoints();
        BigDecimal purePrice = payment.getOrder().getPurePrice();

        messageProducer.sendOrderCancelMessageByUser(bookIds, quantities, couponIds, point, purePrice);
    }
}
