package com.concertticket.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    // "refresh:{userId}" 형식 — userId당 슬롯 하나 (새 로그인이 이전 토큰을 덮어씀)
    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    // application.yml: jwt.refresh-token-expiry (7일 = 604800000ms)
    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    public void save(Long userId, String refreshToken) {
        // TTL을 refresh-token-expiry와 동기화 — Redis가 자동으로 만료 처리
        redisTemplate.opsForValue()
                .set(KEY_PREFIX + userId, refreshToken, refreshTokenExpiry, TimeUnit.MILLISECONDS);
    }

    public Optional<String> findByUserId(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + userId));
    }

    // 로그아웃 시 Refresh Token 즉시 폐기
    public void deleteByUserId(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}