package com.yes25.yes255orderpaymentserver.application.service.strategy;

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
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategy getStrategy(String paymentProvider) {
        PaymentStrategy paymentStrategy = strategies.get(paymentProvider);
        if (Objects.isNull(paymentStrategy)) {
            log.error("해당하는 결제 타입이 존재하지 않습니다. paymentProvider : {}", paymentProvider);
            throw new ApplicationException(ErrorStatus.toErrorStatus(
                "해당하는 결제 타입이 존재하지 않습니다. paymentProvider :" + paymentProvider, 404, LocalDateTime.now()
            ));
        }

        return paymentStrategy;
    }
}
