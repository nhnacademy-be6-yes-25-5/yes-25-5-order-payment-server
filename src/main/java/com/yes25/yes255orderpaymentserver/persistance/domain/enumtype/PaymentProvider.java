package com.yes25.yes255orderpaymentserver.persistance.domain.enumtype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentProvider {
    TOSS,
    NAVER,
    KAKAO;

    @JsonCreator
    public static PaymentProvider from(String value) {
        return PaymentProvider.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
}
