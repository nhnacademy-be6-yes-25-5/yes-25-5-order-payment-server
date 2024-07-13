package com.yes25.yes255orderpaymentserver.application.dto.request;

import lombok.Builder;

@Builder
public record UpdateUserCartQuantityRequest(Long bookId, Integer quantity, String cartId) {

    public static UpdateUserCartQuantityRequest of(Long bookId, Integer quantity, String cartId) {
        return UpdateUserCartQuantityRequest.builder()
            .bookId(bookId)
            .quantity(quantity)
            .cartId(cartId)
            .build();
    }
}
