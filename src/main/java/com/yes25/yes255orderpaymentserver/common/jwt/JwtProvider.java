package com.yes25.yes255orderpaymentserver.common.jwt;

import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import com.yes25.yes255orderpaymentserver.infrastructure.adaptor.KeyManagerAdaptor;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.JwtAuthResponse;
import com.yes25.yes255orderpaymentserver.presentation.dto.response.KeyManagerResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
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

    public JwtAuthResponse getLoginUserFromToken(String token) {
            Claims claims = parseToken(token);

            Long userId = claims.get("userId", Long.class);
            String userRole = claims.get("userRole", String.class);
            String loginStatusName = claims.get("loginStatus", String.class);

            return new JwtAuthResponse(userId, userRole, loginStatusName);
    }
}
