package com.yes25.yes255orderpaymentserver.application.service.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.PaymentStrategyProvider;
import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.impl.TossPayment;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreatePaymentRequest;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.CreatePaymentResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentContextTest {

    @Mock
    private PaymentStrategyProvider paymentStrategyProvider;

    @Mock
    private TossPayment tossPayment;

    @InjectMocks
    private PaymentContext paymentContext;

    private CreatePaymentRequest createPaymentRequest;

    @BeforeEach
    void setUp() {
        createPaymentRequest = new CreatePaymentRequest(
            "key",
            "order",
            "10000",
            PaymentProvider.TOSS,
            List.of(1L),
            List.of(1));
    }

    @DisplayName("전략을 통해 주문을 생성하는지 확인한다.")
    @Test
    void createPayment() {
        // given
        when(paymentStrategyProvider.getStrategy(anyString())).thenReturn(tossPayment);
        when(tossPayment.createPayment(any(CreatePaymentRequest.class))).thenReturn(new CreatePaymentResponse(200));

        // when
        CreatePaymentResponse response = paymentContext.createPayment(createPaymentRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);
    }

    @DisplayName("전략을 통해 0원 결제를 시도하는지 확인한다.")
    @Test
    void createPaymentByZeroAmount() {
        // given
        when(paymentStrategyProvider.getStrategy(anyString())).thenReturn(tossPayment);
        when(tossPayment.createPaymentByZeroAmount(any(CreatePaymentRequest.class))).thenReturn(new CreatePaymentResponse(200));

        // when
        CreatePaymentResponse response = paymentContext.createPaymentByZeroAmount(createPaymentRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);
    }

    @DisplayName("전략을 통해 주문 취소를 하는지 확인한다.")
    @Test
    void cancelPayment() {
        // given
        when(paymentStrategyProvider.getStrategy(anyString())).thenReturn(tossPayment);
        doNothing().when(tossPayment).cancelPayment(anyString(), anyString(), anyInt(), anyString());

        // when
        paymentContext.cancelPayment("key", "reason", 1000, "order", "toss");

        // then
        verify(tossPayment, times(1)).cancelPayment(anyString(), anyString(), anyInt(), anyString());
    }
}