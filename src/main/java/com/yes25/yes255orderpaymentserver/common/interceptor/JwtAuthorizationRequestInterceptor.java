package com.yes25.yes255orderpaymentserver.common.interceptor;


import com.yes25.yes255orderpaymentserver.common.exception.TokenCookieMissingException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationRequestInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && !token.isEmpty()) {
            log.debug("Adding Authorization header: Bearer {}", token);
            template.header(HttpHeaders.AUTHORIZATION, token);
        } else {
            log.warn("Authorization token is missing in the request headers.");
            throw new TokenCookieMissingException(
                ErrorStatus.toErrorStatus("요청 헤더에서 토큰을 찾을 수 없습니다.", 404, LocalDateTime.now())
            );
        }
    }

}