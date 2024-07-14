package com.yes25.yes255orderpaymentserver.common.jwt;

import com.yes25.yes255orderpaymentserver.common.exception.JwtException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.AuthAdaptor;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.JwtAuthResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final AuthAdaptor authAdaptor;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        if ((path.startsWith("/payments") || path.startsWith("/policies")) && StringUtils.isEmpty(request.getHeader("Authorization"))) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.equals("/orders") && StringUtils.isEmpty(request.getHeader("Authorization"))) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.equals("/orders/logs") || path.startsWith("/orders/none") || path.startsWith("/swagger") || path.contains("/v3")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.matches(".*/orders/.*/delivery.*") && StringUtils.isEmpty(request.getHeader("Authorization"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getToken(request);
        String uuid = jwtProvider.getUserNameFromToken(token);
        JwtAuthResponse jwtAuthResponse = authAdaptor.getUserInfoByUUID(uuid);

        JwtUserDetails jwtUserDetails = JwtUserDetails.of(jwtAuthResponse.customerId(),
            jwtAuthResponse.role(), token, jwtAuthResponse.refreshJwt());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            jwtUserDetails, null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + jwtAuthResponse.role()))
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        throw new JwtException(
            ErrorStatus.toErrorStatus("헤더에서 토큰을 찾을 수 없습니다.", 401, LocalDateTime.now())
        );
    }
}
