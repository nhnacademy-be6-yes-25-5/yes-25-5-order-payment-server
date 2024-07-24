package com.yes25.yes255orderpaymentserver.common.utils;

import com.yes25.yes255orderpaymentserver.common.exception.JwtException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtProvider;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.AuthAdaptor;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.JwtAuthResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncSecurityContextUtils {

    private final JwtProvider jwtProvider;
    private final AuthAdaptor authAdaptor;

    public void configureSecurityContext(Message message) {
        MessageProperties properties = message.getMessageProperties();
        String authToken = (String) properties.getHeaders().get("Authorization");

        if (Objects.isNull(authToken)) {
            log.error("토큰이 비어있습니다.");
        }

        String token = getToken(authToken);
        String uuid = jwtProvider.getUserNameFromToken(token);
        JwtAuthResponse jwtAuthResponse = authAdaptor.getUserInfoByUUID(uuid);

        JwtUserDetails jwtUserDetails = JwtUserDetails.of(jwtAuthResponse.customerId(),
            jwtAuthResponse.role(), token, (String) properties.getHeaders().get("Refresh-Token"));

        UsernamePasswordAuthenticationToken newAuthToken = new UsernamePasswordAuthenticationToken(
            jwtUserDetails, null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + jwtAuthResponse.role()))
        );

        SecurityContextHolder.getContext().setAuthentication(newAuthToken);
    }

    private String getToken(String bearer) {
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        throw new JwtException(
            ErrorStatus.toErrorStatus("헤더에서 토큰을 찾을 수 없습니다.", 401, LocalDateTime.now())
        );
    }
}
