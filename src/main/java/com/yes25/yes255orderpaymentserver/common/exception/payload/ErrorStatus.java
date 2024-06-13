package com.yes25.yes255orderpaymentserver.common.exception.payload;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Builder
public record ErrorStatus(String message, int status,
                          @JsonSerialize(using = LocalDateTimeSerializer.class)
                          @JsonDeserialize(using = LocalDateTimeDeserializer.class) LocalDateTime timestamp) {

    public static ErrorStatus toErrorStatus(String message, int status, LocalDateTime timeStamp) {
        return ErrorStatus.builder()
            .message(message)
            .status(status)
            .timestamp(timeStamp)
            .build();
    }

    public HttpStatusCode toHttpStatus() {
        return HttpStatus.valueOf(status);
    }
}
