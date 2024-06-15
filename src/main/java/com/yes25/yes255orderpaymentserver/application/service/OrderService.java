package com.yes25.yes255orderpaymentserver.application.service;

import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;

public interface OrderService {

    boolean processPayment(PreOrder fakeOrder);
}
