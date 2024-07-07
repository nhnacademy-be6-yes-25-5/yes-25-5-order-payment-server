package com.yes25.yes255orderpaymentserver.presentation.dto.request;


import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;

public record CancelOrderRequest(CancelStatus status) {

}
