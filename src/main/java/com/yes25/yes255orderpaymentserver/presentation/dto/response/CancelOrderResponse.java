package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;
import lombok.Builder;

@Builder
public record CancelOrderResponse(CancelStatus status) {

    public static CancelOrderResponse from(CancelStatus cancelStatus) {
        return CancelOrderResponse.builder()
            .status(cancelStatus)
            .build();
    }
}
