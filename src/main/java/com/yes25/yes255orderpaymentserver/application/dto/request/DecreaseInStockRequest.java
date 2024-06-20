package com.yes25.yes255orderpaymentserver.application.dto.request;


import lombok.Builder;

@Builder
public record DecreaseInStockRequest(Long bookId, Integer quantity) {

    public static DecreaseInStockRequest of(Long bookId, Integer quantity) {
        return DecreaseInStockRequest.builder()
            .bookId(bookId)
            .quantity(quantity)
            .build();
    }
}
