package com.yes25.yes255orderpaymentserver.application.service.scheduler;

import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderService orderService;

    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(cron = "0 * * * * ?")
    public void updateOrderStatusScheduler() {
        orderService.updateOrderStatusToDone();
    }
}
