package com.yes25.yes255orderpaymentserver.presentation.dto.response;

public record KeyManagerResponse(Header header, Body body) {

    public static record Header(int resultCode, String resultMessage, boolean isSuccessful) {
    }

    public static record Body(String secret) {
    }
}