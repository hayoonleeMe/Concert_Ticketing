package com.concertticket.auth;

import com.concertticket.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.lang.annotation.Documented;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    // application.yml에서 주입 (${JWT_SECRET})
    @Value("${jwt.secret}")
    private String secretKeyString;

    // ms 단위 (30분 = 1,800,000)
    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // 빈 생성 시 1회만 실행 → 매 요청마다 key를 재생성하는 낭비 방지
        // Base64 → byte[]
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        // byte[] → SecretKey
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, String email, UserRole role) {
        return Jwts.builder()
                // sub: userId (이메일 변경에도 식별자 불변)
                .subject(String.valueOf(userId))
                .claim("email", email)
                // role 포함: 매 요청 DB 조회 없이 권한 확인
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                // HMAC-SHA256 서명 (키 길이로 알고리즘 자동 결정)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    // 0.12.x API: 구버전 setSigningKey() 대신 verifyWith()
                    .verifyWith(key)
                    .build()
                    // 서명 검증 + 만료 시간 검증 동시 수행
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료는 정상적인 흐름이므로 warn
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            // 변조/형식오류
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                // 0.12.x: 구버전 getBody() 대신 getPayload()
                .getPayload();
    }

    public Long getUserId(String token) {
        // sub 클레임 → Long
        return Long.parseLong(getClaims(token).getSubject());
    }

    // Refresh Token은 subject(userId)만 포함 — email·role 클레임 제외
    // Access Token 탈취와 독립적으로 Refresh Token 탈취 피해 범위를 최소화
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                // sub만 포함 — 만료 후 재발급 시 userId로 User 조회
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                // refreshTokenExpiry: Redis TTL과 동일한 값으로 동기화 (7일 = 604800000ms)
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                // HMAC-SHA256 서명 (키 길이로 알고리즘 자동 결정)
                .signWith(key)
                .compact();
    }
}