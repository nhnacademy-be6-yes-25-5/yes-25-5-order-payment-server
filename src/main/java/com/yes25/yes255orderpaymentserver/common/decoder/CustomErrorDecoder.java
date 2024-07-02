package com.yes25.yes255orderpaymentserver.common.decoder;

import com.yes25.yes255orderpaymentserver.common.exception.FeignClientException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String responseBody = null;
        try {
            responseBody = response.body() != null ? new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8) : "알 수 없는 에러";
        } catch (IOException e) {
            log.error("응답 본문을 읽는 중 에러 발생", e);
        }

        return handleException(response, responseBody);
    }

    private Exception handleException(Response response, String responseBody) {
        logResponseDetails(response, responseBody);

        if (response.status() == 400) {
            return throwFeignClientException(response, responseBody);
        } else if (response.status() == 500) {
            return throwFeignClientException(response, responseBody);
        } else {
            return throwFeignClientException(response, responseBody);
        }
    }

    private void logResponseDetails(Response response, String responseBody) {
        String logMessage = "클라이언트 요청에서 에러가 발생하였습니다. "
            + "상태 코드: " + response.status()
            + ", 응답 본문: " + responseBody
            + ", 헤더: " + response.headers()
            + ", 요청: " + response.request();
        log.error(logMessage);
    }

    private FeignClientException throwFeignClientException(Response response, String responseBody) {
        return new FeignClientException(
            ErrorStatus.toErrorStatus(responseBody, response.status(), LocalDateTime.now()));
    }
}
