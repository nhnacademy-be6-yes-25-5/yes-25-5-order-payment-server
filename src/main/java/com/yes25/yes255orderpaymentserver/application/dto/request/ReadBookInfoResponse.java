package com.yes25.yes255orderpaymentserver.application.dto.request;

import com.yes25.yes255orderpaymentserver.presentation.dto.response.ReadPaymentOrderResponse;
import java.math.BigDecimal;

public record ReadBookInfoResponse(Long bookId,
                                   String bookName,
                                   BigDecimal bookPrice,
                                   Boolean bookIsPackable,
                                   String bookImage) {
}
