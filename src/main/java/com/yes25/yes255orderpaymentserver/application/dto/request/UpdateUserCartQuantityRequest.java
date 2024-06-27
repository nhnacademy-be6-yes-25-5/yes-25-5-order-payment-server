package com.yes25.yes255orderpaymentserver.application.dto.request;

import lombok.Builder;

@Builder
public record UpdateUserCartQuantityRequest(Long bookId, Integer quantity) {

    public static UpdateUserCartQuantityRequest of(Long bookId, Integer quantity) {
        return UpdateUserCartQuantityRequest.builder()
            .bookId(bookId)
            .quantity(quantity)
            .build();
    }
}
