package com.yes25.yes255orderpaymentserver.common.jwt;

import com.yes25.yes255orderpaymentserver.common.exception.JwtException;
import com.yes25.yes255orderpaymentserver.common.exception.payload.ErrorStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String ISSUER = "auth-server";
    private final SecretKey secretKey;

    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValidToken(String token) {
        try {
            Jws<Claims> claimJets = Jwts.parserBuilder().setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);

            Claims claims = claimJets.getBody();

            if (claims.getExpiration().before(new Date())) {
                throw new JwtException(
                    ErrorStatus.toErrorStatus("토큰의 유효시간이 지났습니다.", 401, LocalDateTime.now())
                );
            }

            return true;
        } catch (SignatureException e) {
            throw new JwtException(
                ErrorStatus.toErrorStatus("시크릿키 변경이 감지되었습니다.", 401, LocalDateTime.now())
            );
        }
    }

    public Long getUserNameFromToken(String token) {
        return Long.valueOf((Integer) Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("customerId"));
    }

    public String getRolesFromToken(String token) {
        return (String) Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("userRole");
    }
}
