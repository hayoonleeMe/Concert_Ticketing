package com.concertticket.auth;

import com.concertticket.user.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // Spring 없이 직접 생성 → @Value 필드는 null → ReflectionTestUtils로 강제 주입
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKeyString",
                "dGVzdC1zZWNyZXQta2V5LW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHM=");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", 1800000L);    // 30분 (ms)
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiry", 604800000L); // 7일 (ms)
        // @PostConstruct는 Spring이 호출 → 테스트에서는 직접 init() 호출 필요
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Access Token 정상 생성 — 클레임 검증")
    void generateAccessToken_정상생성() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com", UserRole.USER);

        // JWT 형식: "header.payload.signature" 세 파트
        assertThat(token).isNotBlank();
        Claims claims = jwtTokenProvider.getClaims(token);
        // sub → userId
        assertThat(Long.parseLong(claims.getSubject())).isEqualTo(1L);
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        // enum.name()으로 저장됨
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    @DisplayName("만료된 토큰 — validateToken false 반환")
    void validateToken_만료된토큰() {
        // expiry를 음수로 설정 → 생성 즉시 과거 시간으로 만료
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", -1L);
        String expiredToken = jwtTokenProvider.generateAccessToken(1L, "test@example.com", UserRole.USER);

        // validateToken 내부에서 ExpiredJwtException catch → false 반환
        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("변조된 토큰 — validateToken false 반환")
    void validateToken_변조된토큰() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com", UserRole.USER);
        // Signature 부분에 임의 문자열 추가 → HMAC 서명 불일치 → JwtException catch
        assertThat(jwtTokenProvider.validateToken(token + "tampered")).isFalse();
    }

    @Test
    @DisplayName("getUserId — sub 클레임에서 Long 파싱")
    void getUserId_정상파싱() {
        String token = jwtTokenProvider.generateAccessToken(42L, "test@example.com", UserRole.USER);
        // sub는 String으로 저장됨 → Long.parseLong()으로 변환하여 반환
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(42L);
    }
}
