package com.yes25.yes255orderpaymentserver.application.service.strategy.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.service.strategy.payment.impl.TossPayment;
import com.yes25.yes255orderpaymentserver.common.exception.ApplicationException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentStrategyProviderTest {

    @Mock
    private Map<String, PaymentStrategy> strategies;

    @Mock
    private TossPayment tossPayment;

    @InjectMocks
    private PaymentStrategyProvider paymentStrategyProvider;

    @DisplayName("결제 전략을 성공적으로 찾는지 확인한다.")
    @Test
    void getStrategy() {
        // given
        when(strategies.get(anyString())).thenReturn(tossPayment);

        // when
        PaymentStrategy paymentStrategy = paymentStrategyProvider.getStrategy("toss");

        // then
        assertThat(paymentStrategy).isExactlyInstanceOf(TossPayment.class);
    }

    @DisplayName("결제 전략을 찾지 못하면 예외를 발생시키는지 확인한다.")
    @Test
    void getStrategyWhenNotFoundStrategy() {
        // given
        when(strategies.get(anyString())).thenReturn(null);

        // when && then
        assertThatThrownBy(() -> paymentStrategyProvider.getStrategy("toss"))
            .isInstanceOf(ApplicationException.class);
    }
}