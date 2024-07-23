package com.yes25.yes255orderpaymentserver.application.service.strategy.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.application.service.strategy.status.impl.CancelStatusStrategy;
import com.yes25.yes255orderpaymentserver.common.exception.ApplicationException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderStatusStrategyProviderTest {

    @Mock
    private CancelStatusStrategy cancelStatusStrategy;

    @Mock
    private Map<String, OrderStatusStrategy> strategies;

    @InjectMocks
    private OrderStatusStrategyProvider orderStatusStrategyProvider;

    @DisplayName("주문 상태 전략을 찾는지 확인한다.")
    @Test
    void getStrategy() {
        // given
        when(strategies.get(anyString())).thenReturn(cancelStatusStrategy);

        // when
        OrderStatusStrategy orderStatusStrategy = orderStatusStrategyProvider.getStrategy("cancel");

        // then
        assertThat(orderStatusStrategy).isExactlyInstanceOf(CancelStatusStrategy.class);
    }

    @DisplayName("주문 상태 전략을 찾지 못하면 예외를 반환하는지 확인한다.")
    @Test
    void getStrategyWhenNotFoundOrderStatusStrategy() {
        // given
        when(strategies.get(anyString())).thenReturn(null);

        // when && then
        assertThatThrownBy(() -> orderStatusStrategyProvider.getStrategy("todo"))
            .isInstanceOf(ApplicationException.class);
    }
}