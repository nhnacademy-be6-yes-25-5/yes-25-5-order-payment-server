package com.yes25.yes255orderpaymentserver.application.service.strategy.status;

import com.yes25.yes255orderpaymentserver.common.exception.ApplicationException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusStrategyProvider {

    private final Map<String, OrderStatusStrategy> strategies;

    public OrderStatusStrategy getStrategy(String orderStatusName) {
        OrderStatusStrategy orderStatusStrategy = strategies.get(orderStatusName);
        if (Objects.isNull(orderStatusStrategy)) {
            log.error("해당하는 주문 상태가 존재하지 않습니다. paymentProvider : {}", orderStatusName);
            throw new ApplicationException(ErrorStatus.toErrorStatus(
                "해당하는 주문 상태가 존재하지 않습니다. paymentProvider :" + orderStatusName, 404, LocalDateTime.now()
            ));
        }

        return orderStatusStrategy;
    }

}
