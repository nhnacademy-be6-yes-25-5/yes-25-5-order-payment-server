package com.yes25.yes255orderpaymentserver.application.service.strategy.payment.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.CancelPaymentRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.PaymentRetryService;
import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.PaymentStrategy;
import com.yes25.yes255orderpaymentserver.common.exception.EntityNotFoundException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.KeyManagerAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.TossAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderBook;
import com.yes25.yes255orderpaymentserver.persistance.domain.OrderCoupon;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.PaymentDetail;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentDetailRepository;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.KeyManagerResponse;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("toss")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TossPayment implements PaymentStrategy {

    private final MessageProducer messageProducer;
    private final TossAdaptor tossAdaptor;
    private final BookAdaptor bookAdaptor;
    private final KeyManagerAdaptor keyManagerAdaptor;

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;

    private final PaymentRetryService paymentRetryService;

    private String paymentSecretKey;

    @Value("${payment.secret}")
    private String secretKeyId;

    @PostConstruct
    public void init() {
        KeyManagerResponse response = keyManagerAdaptor.getSecret(secretKeyId);

        if (Objects.nonNull(response.body())) {
            paymentSecretKey = response.body().secret();
        }
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        checkBookStock(request);

        return processingPayment(request);
    }

    @Override
    public void cancelPayment(String paymentKey, String cancelReason, Integer cancelAmount,
        String orderId) {
        CancelPaymentRequest request = CancelPaymentRequest.from(cancelReason, cancelAmount);
        String authorization = "Basic " + Base64.getEncoder().encodeToString((paymentSecretKey + ":").getBytes());

        tossAdaptor.cancelPayment(paymentKey, request, authorization, orderId);
    }

    @Override
    public CreatePaymentResponse createPaymentByZeroAmount(CreatePaymentRequest request) {
        checkBookStock(request);

        Payment payment = request.toEntity();
        Payment savedPayment = paymentRepository.save(payment);

        PaymentDetail paymentDetail = request.zeroPay(savedPayment);
        paymentDetailRepository.save(paymentDetail);

        log.info("결제가 성공적으로 이루어졌습니다. {}", payment);
        sendPaymentDoneMessage(payment, request);

        return new CreatePaymentResponse(200);
    }

    private CreatePaymentResponse processingPayment(CreatePaymentRequest request) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode(
            (paymentSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        JSONObject obj = new JSONObject();
        obj.put("paymentKey", request.paymentKey());
        obj.put("orderId", request.orderId());
        obj.put("amount", request.amount());

        Payment payment = savePayment(request);
        sendPaymentDoneMessage(payment, request);

        try {
            JSONObject response = tossAdaptor.confirmPayment(authorizations, obj.toString());
            PaymentDetail paymentDetail = PaymentDetail.from(response, payment);
            paymentDetailRepository.save(paymentDetail);

            log.info("결제가 성공적으로 이루어졌습니다. {}", payment);
        } catch (Exception e) {
            log.error("결제 승인 중 에러 발생 : {}", e.getMessage());
            CompletableFuture.runAsync(new DelegatingSecurityContextRunnable(
                () -> paymentRetryService.retryPaymentConfirm(request, authorizations, obj, 0, payment.getPaymentId())));
        }

        return new CreatePaymentResponse(200);
    }

    private void checkBookStock(CreatePaymentRequest request) {
        StockRequest stockRequest = StockRequest.of(request.bookIds(),
            request.quantities(), OperationType.DECREASE);
        bookAdaptor.updateStock(stockRequest);
    }

    private void sendPaymentDoneMessage(Payment payment, CreatePaymentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authToken;

        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof JwtUserDetails jwtUserDetails) {
                authToken = "Bearer " + jwtUserDetails.accessToken();
            } else {
                authToken = null;
            }
        } else {
            authToken = null;
        }

        SuccessPaymentResponse response = SuccessPaymentResponse.of(payment, request);
        messageProducer.sendMessage("payExchange", "payRoutingKey", response, authToken);
    }

    private Payment savePayment(CreatePaymentRequest request) {
        Payment payment = request.toEntity();

        return paymentRepository.save(payment);
    }
}
