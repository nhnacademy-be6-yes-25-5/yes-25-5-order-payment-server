package com.yes25.yes255orderpaymentserver.presentation.dto.request;

import java.util.List;

public record CreatePaymentRequest(String paymentKey,
                                   String orderId,
                                   String amount,
                                   List<Long> bookIds,
                                   List<Integer> quantities) {

}
