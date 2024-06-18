package com.yes25.yes255orderpaymentserver.presentation.dto;

public record ApiResponse<T> (T body, int status) {

    public static <T> ApiResponse<T> ok(T body) {
        return new ApiResponse<>(body, 200);
    }

    public static <T> ApiResponse<T> created(T body) {
        return new ApiResponse<>(body, 201);
    }

    public static <T> ApiResponse<T> ok(T body, int status) {
        return new ApiResponse<>(body, status);
    }
}
