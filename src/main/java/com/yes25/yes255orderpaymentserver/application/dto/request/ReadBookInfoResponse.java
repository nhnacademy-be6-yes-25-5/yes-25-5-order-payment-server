package com.yes25.yes255orderpaymentserver.application.dto.request;

import java.math.BigDecimal;

public record ReadBookInfoResponse(Long bookId,
                                   String bookName,
                                   BigDecimal bookPrice,
                                   Boolean bookIsPackable,
                                   String bookImage) {
}
