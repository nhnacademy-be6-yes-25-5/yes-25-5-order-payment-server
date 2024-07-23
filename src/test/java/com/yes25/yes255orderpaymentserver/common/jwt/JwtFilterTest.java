package com.yes25.yes255orderpaymentserver.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yes25.yes255orderpaymentserver.common.exception.JwtException;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.AuthAdaptor;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.JwtAuthResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthAdaptor authAdaptor;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.setContext(new SecurityContextImpl());
    }

    @ParameterizedTest
    @DisplayName("Authorization 헤더가 없는 경우 필터를 통과하는지 확인한다.")
    @ValueSource(strings = {"/payments", "/orders", "/orders/logs"})
    void shouldPassFilterWhenNoAuthorizationHeaderForPaymentsAndPolicies(String path) throws ServletException, IOException {
        // given
        request.setServletPath(path);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증을 설정하는지 확인한다.")
    void shouldSetAuthenticationWithValidJwtToken()
        throws IOException, ServletException {
        // given
        request.setServletPath("/orders");
        request.addHeader("Authorization", "Bearer valid-token");

        String uuid = "uuid";
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse(1L, "USER", "active", "refreshToken");

        when(jwtProvider.getUserNameFromToken(anyString())).thenReturn(uuid);
        when(authAdaptor.getUserInfoByUUID(anyString())).thenReturn(jwtAuthResponse);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 없는 경우 JwtException을 던지는지 확인한다.")
    void shouldThrowJwtExceptionWhenNoAuthorizationHeader() {
        // given
        request.setServletPath("/orders/orderId");

        // when
        assertThatThrownBy(() -> jwtFilter.doFilterInternal(request, response, filterChain))
            .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Authorization 헤더가 잘못된 경우 JwtException을 던지는지 확인한다.")
    void shouldThrowJwtExceptionWhenInvalidAuthorizationHeader() {
        // given
        request.setServletPath("/orders");
        request.addHeader("Authorization", "InvalidHeader");

        // when
        Throwable thrown = catchThrowable(() -> jwtFilter.doFilterInternal(request, response, filterChain));

        // then
        assertThat(thrown).isInstanceOf(JwtException.class);
        assertThat(((JwtException) thrown).getErrorStatus().message()).isEqualTo("헤더에서 토큰을 찾을 수 없습니다.");
    }
}
