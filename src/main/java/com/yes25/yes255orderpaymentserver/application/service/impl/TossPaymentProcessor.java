package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.CancelPaymentRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.PaymentProcessor;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.KeyManagerAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.TossAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.KeyManagerResponse;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TossPaymentProcessor implements PaymentProcessor {

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
        paymentSecretKey = response.body().secret();
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
        String widgetSecretKey = paymentSecretKey;
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode(
            (widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        HttpURLConnection connection = configureUrl(authorizations);

        try (OutputStream outputStream = connection.getOutputStream()) {
            JSONObject obj = new JSONObject();
            obj.put("paymentKey", request.paymentKey());
            obj.put("orderId", request.orderId());
            obj.put("amount", request.amount());

            outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));

            int code = connection.getResponseCode();
            boolean isSuccess = code == 200;

            InputStream responseStream =
                isSuccess ? connection.getInputStream() : connection.getErrorStream();

            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            responseStream.close();

            if (isSuccess) {
                Payment payment = savePayment(jsonObject);
                log.info("결제가 성공적으로 이루어졌습니다. {}", payment);
                sendPaymentDoneMessage(payment, request);
            }
        } catch (Exception e) {
            log.error("error : ", e);
            return new CreatePaymentResponse(500);
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

        MessagePostProcessor messagePostProcessor = message -> {
            if (authToken != null) {
                message.getMessageProperties().setHeader("Authorization", authToken);
            }
            return message;
        };

        SuccessPaymentResponse response = SuccessPaymentResponse.of(payment, request);

        rabbitTemplate.convertAndSend("payExchange", "payRoutingKey", response, messagePostProcessor);
    }

    private Payment savePayment(JSONObject jsonObject) {
        Payment payment = Payment.from(jsonObject);

        return paymentRepository.save(payment);
    }

    private HttpURLConnection configureUrl(String authorizations) {
        HttpURLConnection connection;

        try {
            URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }
}
