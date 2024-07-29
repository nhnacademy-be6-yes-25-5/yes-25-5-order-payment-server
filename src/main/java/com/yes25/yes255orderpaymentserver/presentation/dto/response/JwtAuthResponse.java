package com.yes25.yes255orderpaymentserver.presentation.dto.response;

import lombok.Builder;

@Builder
public record JwtAuthResponse(Long customerId,
                              String role,
                              String loginStateName) {

}
