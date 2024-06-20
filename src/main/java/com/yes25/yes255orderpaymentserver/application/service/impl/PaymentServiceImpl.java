package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.dto.response.SuccessPaymentResponse;
import com.yes25.yes255orderpaymentserver.application.service.PaymentService;
import com.yes25.yes255orderpaymentserver.common.exception.FeignClientException;
import com.yes25.yes255orderpaymentserver.common.exception.StockUnavailableException;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final RabbitTemplate rabbitTemplate;
    private final PaymentRepository paymentRepository;
    private final BookAdaptor bookAdaptor;

    @Value("${payment.secret}")
    private String paymentSecretKey;

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        try {
            checkAndDecreaseInStock(request);
        } catch (FeignClientException e) {
            throw new StockUnavailableException(e.getErrorStatus(), request.orderId());
        }

        return processingPayment(request);
    }

    @Override
    public void cancelPayment(String paymentKey, String message) {

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
                sendPaymentDoneMessage(payment);
            }
        } catch (Exception e) {
            log.error("error : ", e);
            return new CreatePaymentResponse(500);
        }

        return new CreatePaymentResponse(200);
    }

    private void checkAndDecreaseInStock(CreatePaymentRequest paymentRequest) {
        StockRequest stockRequest = StockRequest.of(paymentRequest, OperationType.DECREASE);

        bookAdaptor.updateStock(stockRequest);
    }

    private void sendPaymentDoneMessage(Payment payment) {
        SuccessPaymentResponse response = SuccessPaymentResponse.fromEntity(payment);

        rabbitTemplate.convertAndSend("paymentExchange", "paymentRoutingKey", response);
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
