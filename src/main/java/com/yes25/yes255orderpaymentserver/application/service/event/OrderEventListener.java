package com.yes25.yes255orderpaymentserver.application.service.event;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.service.PaymentService;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final BookAdaptor bookAdaptor;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onOrderCreatEventAfterRollback(OrderCreateEvent event) {
        log.info("주문이 롤백되었습니다. 재고를 복구합니다. 책 ID : {}, 재고 : {}", event.bookIdList(), event.quantityList());

        increaseBooks(event);
        cancelPayment(event);
    }

    private void cancelPayment(OrderCreateEvent event) {
        paymentService.cancelPayment(event.paymentKey(), "주문 롤백으로 인한 취소", event.cancelAmount(),
            event.orderId());
    }

    private void increaseBooks(OrderCreateEvent event) {
        StockRequest stockRequest = StockRequest.of(event.bookIdList(), event.quantityList(),
            OperationType.INCREASE);

        bookAdaptor.updateStock(stockRequest);
    }
}
