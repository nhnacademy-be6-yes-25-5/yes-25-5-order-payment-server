package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import java.time.LocalDateTime;

public class OrderStatusNotFoundException extends EntityNotFoundException {

    public OrderStatusNotFoundException(String name) {
        super(ErrorStatus.toErrorStatus("해당하는 주문 상태를 찾을 수 없습니다. 주문 상태명 : " + name, 404, LocalDateTime.now()));
    }
}
