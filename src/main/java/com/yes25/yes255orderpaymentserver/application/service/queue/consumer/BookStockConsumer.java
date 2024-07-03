package com.yes25.yes255orderpaymentserver.application.service.queue.consumer;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.application.service.queue.producer.MessageProducer;
import com.yes25.yes255orderpaymentserver.common.utils.AsyncSecurityContextUtils;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.BookAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookStockConsumer {

    private final BookAdaptor bookAdaptor;
    private final AsyncSecurityContextUtils securityContextUtils;
    private final MessageProducer messageProducer;

    @RabbitListener(queues = "stockDecreaseQueue")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000))
    public void receiveStockDecreaseQueue(StockRequest stockRequest, Message message) {
        log.info("주문이 확정되어 도서 재고를 감소시킵니다. 요청 내용: {}", stockRequest);
        securityContextUtils.configureSecurityContext(message);
        bookAdaptor.updateStock(stockRequest);

        log.info("도서 재고 감소가 성공적으로 처리되었습니다. 요청 내용: {}", stockRequest);
    }

    @RabbitListener(queues = "stockIncreaseQueue")
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000))
    public void receiveStockIncreaseQueue(StockRequest stockRequest, Message message) {
        log.info("결제가 취소되어 도서 재고를 롤백합니다. 요청 내용: {}", stockRequest);
        securityContextUtils.configureSecurityContext(message);
        bookAdaptor.updateStock(stockRequest);

        log.info("도서 재고 롤백이 성공적으로 처리되었습니다. 요청 내용: {}", stockRequest);
    }

    @Recover
    public void cartRecover(Throwable throwable, StockRequest stockRequest) {
        if (stockRequest.operationType().equals(OperationType.DECREASE)) {
            log.error("도서 재고 감소에 실패하였습니다. 예외 : {}", throwable.getMessage());

            messageProducer.sendDlxMessage("dlxExchange", "dlx.stockDecreaseQueue", stockRequest);
        } else {
            log.error("도서 재고 롤백에 실패하였습니다. 예외 : {}", throwable.getMessage());

            messageProducer.sendDlxMessage("dlxExchange", "dlx.stockIncreaseQueue", stockRequest);
        }
    }
}
