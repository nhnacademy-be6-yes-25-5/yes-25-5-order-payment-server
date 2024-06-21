package com.yes25.yes255orderpaymentserver.application.service.queue;

import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointRequest;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.UserAdaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointConsumer {

    private final UserAdaptor userAdaptor;

    @RabbitListener(queues = "orderDoneQueue")
    private void updatePoints(UpdatePointMessage updatePointMessage) {
        UpdatePointRequest updatePointRequest = UpdatePointRequest.from(updatePointMessage);
        userAdaptor.updatePoint(updatePointMessage.userId(), updatePointRequest);
    }
}
