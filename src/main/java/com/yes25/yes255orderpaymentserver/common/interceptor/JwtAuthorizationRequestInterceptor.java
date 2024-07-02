package com.yes25.yes255orderpaymentserver.common.interceptor;


import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String url = template.url();

        if (!url.matches(".*/payments/.*/cancel.*")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                && authentication.getPrincipal() instanceof JwtUserDetails userDetails) {
                template.header("Authorization", "Bearer " + userDetails.accessToken());
            }
        }
    }

}