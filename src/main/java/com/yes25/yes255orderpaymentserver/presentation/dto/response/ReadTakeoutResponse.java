package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import com.yes25.yes255orderpaymentserver.persistance.domain.Takeout;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.TakeoutType;
import lombok.Builder;

@Builder
public record ReadTakeoutResponse(TakeoutType takeoutType, Integer takeoutPrice, String takeoutDescription) {

    public static ReadTakeoutResponse fromEntity(Takeout takeout) {
        return ReadTakeoutResponse.builder()
            .takeoutType(TakeoutType.valueOf(takeout.getTakeoutName()))
            .takeoutPrice(takeout.getTakeoutPrice().intValue())
            .takeoutDescription(takeout.getTakeoutDescription())
            .build();
    }
}
