package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import lombok.Builder;

@Builder
public record ReadPaymentOrderResponse(Long bookId, String bookName,
                                       String bookAuthor, Integer bookPrice, String bookImage, Integer bookQuantity) {

    public static ReadPaymentOrderResponse fromDto(ReadBookResponse readBookResponse) {
        return ReadPaymentOrderResponse.builder()
            .bookId(readBookResponse.bookId())
            .bookName(readBookResponse.bookName())
            .bookAuthor(readBookResponse.bookAuthor())
            .bookPrice(readBookResponse.bookPrice().intValue())
            .bookImage(readBookResponse.bookImage())
            .bookQuantity(readBookResponse.bookQuantity())
            .build();
    }
}
