package com.yes25.yes255orderpaymentserver.common.jwt;

import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.KeyManagerAdaptor;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.JwtAuthResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.KeyManagerResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final KeyManagerAdaptor keyManagerAdaptor;
    private SecretKey secretKey;

    @Value("${jwt.secret}")
    public String secretKeyId;

    @PostConstruct
    public void init() {
        KeyManagerResponse response = keyManagerAdaptor.getSecret(secretKeyId);

        if (Objects.nonNull(response.body())) {
            this.secretKey = Keys.hmacShaKeyFor(
                response.body().secret().getBytes(StandardCharsets.UTF_8));
        }
    }

    public String getUserNameFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public JwtAuthResponse getJwtAuthFromToken(String token) {
            Claims claims = parseToken(token);

            Long customerId = claims.get("userId", Long.class);
            String role = claims.get("userRole", String.class);
            String loginStatusName = claims.get("loginStatus", String.class);

            return JwtAuthResponse.builder()
                    .customerId(customerId)
                    .role(role)
                    .loginStateName(loginStatusName).build();
    }
}
