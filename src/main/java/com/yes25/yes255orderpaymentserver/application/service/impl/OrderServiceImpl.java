package com.yes25.yes255orderpaymentserver.application.service.impl;

import com.yes25.yes255orderpaymentserver.application.service.OrderService;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    /*
     * 1. 주문이 들어온다.
     * 2. 주문 이벤트 발행.
     * 3. 주문 이벤트를 구독하는 곳에서 결제 실행
     * 4. 결제 이벤트가 종료되면 데이터 정합성 확인(재고 확인)
     * */
    @Override
    public boolean processPayment(PreOrder fakeOrder) {
        return false;
    }

}
