package com.yes25.yes255orderpaymentserver.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.dto.request.CancelPaymentRequest;
import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.impl.TossPayment;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.TossAdaptor;
import com.yes25.yes255orderpaymentserver.persistance.domain.Payment;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.persistance.repository.PaymentRepository;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.List;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TossPaymentProcessorTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private BookAdaptor bookAdaptor;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TossAdaptor tossAdaptor;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpURLConnection mockConnection;

    @InjectMocks
    private TossPayment tossPaymentProcessor;

    private CreatePaymentRequest createPaymentRequest;
    private Payment payment;
    private JSONObject jsonObject;
    private JwtUserDetails jwtUserDetails;

    @BeforeEach
    void setUp() {
        createPaymentRequest = new CreatePaymentRequest(
            "paymentKey",
            "orderId",
            "amount",
            PaymentProvider.TOSS,
            List.of(1L, 2L),
            List.of(1, 2)
        );

        payment = Payment.builder()
            .paymentId(1L)
            .paymentKey("paymentKey")
            .paymentAmount(BigDecimal.valueOf(10000))
            .approveAt(LocalDateTime.now())
            .requestedAt(LocalDateTime.now())
            .paymentMethod("카드")
            .preOrderId("orderId")
            .build();

        jsonObject = new JSONObject();
        jsonObject.put("paymentKey", "paymentKey");
        jsonObject.put("totalAmount", 10000);
        jsonObject.put("approvedAt", LocalDateTime.now().toString());
        jsonObject.put("requestedAt", LocalDateTime.now().toString());
        jsonObject.put("method", "카드");
        jsonObject.put("orderId", "orderId");
    }

    @DisplayName("결제를 성공적으로 생성하는지 확인한다.")
    @Test
    void createPayment() {
        // given
        when(tossAdaptor.confirmPayment(anyString(), anyString())).thenReturn(jsonObject);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // when
        CreatePaymentResponse response = tossPaymentProcessor.createPayment(createPaymentRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);
    }

    @DisplayName("결제 취소를 성공적으로 처리하는지 확인한다.")
    @Test
    void cancelPayment() {
        // given
        String paymentKey = "paymentKey";
        String cancelReason = "test reason";
        Integer cancelAmount = 1000;
        String orderId = "orderId";

        // when
        tossPaymentProcessor.cancelPayment(paymentKey, cancelReason, cancelAmount, orderId);

        // then
        verify(tossAdaptor, times(1)).cancelPayment(anyString(), any(CancelPaymentRequest.class), anyString(), anyString());
    }

    @DisplayName("0원 결제를 성공적으로 생성하는지 확인한다.")
    @Test
    void createPaymentByZeroAmount() {
        // given
        jwtUserDetails = mock(JwtUserDetails.class);
        when(jwtUserDetails.accessToken()).thenReturn("testAccessToken");
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwtUserDetails);
        SecurityContextHolder.setContext(securityContext);

        // when
        CreatePaymentResponse response = tossPaymentProcessor.createPaymentByZeroAmount(createPaymentRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);
    }
}
