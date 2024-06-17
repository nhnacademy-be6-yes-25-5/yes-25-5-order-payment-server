package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.TakeoutType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderRequest(String orderId,
                                 Long userId,
                                 List<Long> productIds,
                                 List<Integer> quantities,
                                 List<BigDecimal> prices,
                                 BigDecimal orderTotalAmount,
                                 TakeoutType takeoutType,
                                 String addressRaw,
                                 String addressDetail,
                                 String zipcode,
                                 String reference,
                                 LocalDateTime deliveryDate,
                                 String orderName,
                                 String orderEmail,
                                 String orderPhoneNumber,
                                 String receiveName,
                                 String receiveEmail,
                                 String receivePhoneNumber,
                                 Long couponId,
                                 BigDecimal points) {

}
