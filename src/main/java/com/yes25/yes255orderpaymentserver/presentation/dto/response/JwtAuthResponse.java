package com.yes25.yes255orderpaymentserver.presentation.dto.response;

public record JwtAuthResponse(Long customerId,
                              String role,
                              String loginStateName) {

}
