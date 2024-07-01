package com.yes25.yes255orderpaymentserver.common.exception;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import java.time.LocalDateTime;

public class OrderNotFoundException extends EntityNotFoundException{

    public OrderNotFoundException(String orderId) {
        super(ErrorStatus.toErrorStatus("해당하는 주문을 찾을 수 없습니다. 주문 ID : " + orderId,
            404, LocalDateTime.now()));
    }
}
