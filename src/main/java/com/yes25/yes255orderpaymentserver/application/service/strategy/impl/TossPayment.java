package com.yes25.yes255orderpaymentserver.application.service.strategy.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.CancelPaymentRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.strategy.PaymentStrategy;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.KeyManagerAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.TossAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.KeyManagerResponse;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("toss")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TossPayment implements PaymentStrategy {

    private final RabbitTemplate rabbitTemplate;
    private final PaymentRepository paymentRepository;
    private final TossAdaptor tossAdaptor;
    private final BookAdaptor bookAdaptor;
    private final KeyManagerAdaptor keyManagerAdaptor;

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
        paymentRepository.save(payment);

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

        JSONObject response = tossAdaptor.confirmPayment(authorizations, obj.toString());

        Payment payment = savePayment(response, request.paymentProvider());
        log.info("결제가 성공적으로 이루어졌습니다. {}", payment);
        sendPaymentDoneMessage(payment, request);

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

        MessagePostProcessor messagePostProcessor = message -> {
            if (authToken != null) {
                message.getMessageProperties().setHeader("Authorization", authToken);
            }
            return message;
        };

        SuccessPaymentResponse response = SuccessPaymentResponse.of(payment, request);

        rabbitTemplate.convertAndSend("payExchange", "payRoutingKey", response, messagePostProcessor);
    }

    private Payment savePayment(JSONObject jsonObject, PaymentProvider paymentProvider) {
        Payment payment = Payment.from(jsonObject, paymentProvider);

        return paymentRepository.save(payment);
    }
}
