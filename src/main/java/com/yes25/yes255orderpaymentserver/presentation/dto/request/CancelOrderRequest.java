package com.yes25.yes255orderpaymentserver.presentation.dto.request;


import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.CancelStatus;
import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.PaymentProvider;

public record CancelOrderRequest(CancelStatus status, PaymentProvider paymentProvider) {

}
