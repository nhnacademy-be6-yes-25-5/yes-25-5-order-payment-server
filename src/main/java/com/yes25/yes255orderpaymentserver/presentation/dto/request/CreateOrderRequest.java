package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.TakeoutType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateOrderRequest(String orderId,
                                 List<Long> productIds,
                                 List<Integer> quantities,
                                 List<BigDecimal> prices,
                                 BigDecimal orderTotalAmount,
                                 BigDecimal shippingFee,
                                 BigDecimal takeoutPrice,
                                 BigDecimal discountPrice,
                                 TakeoutType takeoutType,
                                 String addressRaw,
                                 String addressDetail,
                                 String zipcode,
                                 String reference,
                                 LocalDate deliveryDate,
                                 String orderName,
                                 String orderEmail,
                                 String orderPhoneNumber,
                                 String receiveName,
                                 String receiveEmail,
                                 String receivePhoneNumber,
                                 List<Long> couponIds,
                                 BigDecimal points,
                                 String role,
                                 String cartId) {

}
