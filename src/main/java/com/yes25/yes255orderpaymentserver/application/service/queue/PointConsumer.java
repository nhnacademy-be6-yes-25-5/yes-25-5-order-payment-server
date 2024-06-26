package com.yes25.yes255orderpaymentserver.application.service.queue;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointRequest;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointConsumer {

    private final UserAdaptor userAdaptor;

    /**
     * 회원 서버에서 포인트 적립이 수정중이라 주석처리하였습니다.
     * */
    @RabbitListener(queues = "orderDoneQueue")
    private void updatePoints(UpdatePointMessage updatePointMessage) {
        log.info("주문이 확정되어 포인트가 적립됩니다. : {}", updatePointMessage.usePoints());
        UpdatePointRequest updatePointRequest = UpdatePointRequest.from(updatePointMessage);

//        userAdaptor.updatePoint(updatePointRequest);
    }
}
