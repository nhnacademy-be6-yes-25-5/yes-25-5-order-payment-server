package com.yes25.yes255orderpaymentserver.common.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.common.exception.JwtException;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtProvider;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.AuthAdaptor;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.JwtAuthResponse;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AsyncSecurityContextUtilsTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthAdaptor authAdaptor;

    @InjectMocks
    private AsyncSecurityContextUtils securityContextUtils;

    @Mock
    private Message message;

    @Mock
    private MessageProperties messageProperties;

    @BeforeEach
    void setUp() {
        when(message.getMessageProperties()).thenReturn(messageProperties);
        SecurityContextHolder.clearContext();
    }

    @DisplayName("유효한 토큰이 주어졌을 때 보안 컨텍스트가 올바르게 설정되는지 확인한다")
    @Test
    void configureSecurityContext_withValidToken() {
        // given
        String authToken = "Bearer validToken";
        String token = "validToken";
        String uuid = "user-uuid";
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse(1L, "USER", "active", "refreshJwt");

        when(messageProperties.getHeaders()).thenReturn(Collections.singletonMap("Authorization", authToken));
        when(jwtProvider.getUserNameFromToken(token)).thenReturn(uuid);
        when(authAdaptor.getUserInfoByUUID(uuid)).thenReturn(jwtAuthResponse);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            uuid,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // when
        securityContextUtils.configureSecurityContext(message);

        // then
        UsernamePasswordAuthenticationToken authentication =
            (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
    }

    @DisplayName("토큰이 비어 있을 때 로그 에러를 출력하는지 확인한다")
    @Test
    void configureSecurityContext_withNullToken() {
        // given
        when(messageProperties.getHeaders()).thenReturn(Collections.singletonMap("Authorization", null));

        // when
        assertThatThrownBy(() ->
            securityContextUtils.configureSecurityContext(message))
            .isInstanceOf(JwtException.class);
    }

    @DisplayName("유효하지 않은 토큰이 주어졌을 때 예외를 발생시키는지 확인한다")
    @Test
    void getToken_withInvalidToken() {
        // given
        when(messageProperties.getHeaders()).thenReturn(Collections.singletonMap("Authorization", "invalidToken"));

        // when && then
        assertThatThrownBy(() ->
            securityContextUtils.configureSecurityContext(message))
            .isInstanceOf(JwtException.class);
    }

    @DisplayName("Bearer 접두사가 있는 유효한 토큰이 주어졌을 때 올바른 토큰을 추출하는지 확인한다")
    @Test
    void getToken_withValidBearerToken() {
        // given
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse(1L, "USER", "active", "refreshJwt");
        when(messageProperties.getHeaders()).thenReturn(Collections.singletonMap("Authorization", "Bearer validToken"));
        when(jwtProvider.getUserNameFromToken(anyString())).thenReturn("uuid");
        when(authAdaptor.getUserInfoByUUID(anyString())).thenReturn(jwtAuthResponse);

        // when
        assertDoesNotThrow(() ->
            securityContextUtils.configureSecurityContext(message));
    }
}
