package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.Takeout;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderRequest(Long userId,
                                 List<Long> productIds,
                                 BigDecimal orderTotalAmount,
                                 Takeout takeout,
                                 String addressRaw,
                                 String addressDetail,
                                 String zipcode) {

}
