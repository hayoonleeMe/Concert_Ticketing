package com.concertticket.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Spring 컨텍스트 없이 MockitoExtension만 사용 — 빠른 단위 테스트
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    // opsForValue() 체이닝을 지원하기 위해 별도로 선언
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        // deleteByUserId는 opsForValue()를 사용하지 않으므로 lenient stub 사용
        // strict stubbing 기본값에서 미사용 stub이 있으면 UnnecessaryStubbingException 발생
        Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // @Value 필드는 Spring 없이 주입되지 않으므로 ReflectionTestUtils로 직접 설정 (7일 = 604800000ms)
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiry", 604800000L);
    }

    @Test
    void save_올바른_키와_TTL로_저장() {
        // when
        refreshTokenService.save(1L, "my-refresh-token");

        // then
        // "refresh:{userId}" 키 형식, 값, TTL(ms), TimeUnit.MILLISECONDS 순서로 검증
        verify(valueOperations).set("refresh:1", "my-refresh-token", 604800000L, TimeUnit.MILLISECONDS);
    }

    @Test
    void findByUserId_저장된_토큰_반환() {
        // given
        // valueOperations.get()이 저장된 토큰 문자열 반환
        when(valueOperations.get("refresh:1")).thenReturn("stored-token");

        // when
        Optional<String> result = refreshTokenService.findByUserId(1L);

        // then
        // Optional.ofNullable("stored-token") → Optional.of("stored-token") 확인
        assertThat(result).isPresent().contains("stored-token");
    }

    @Test
    void findByUserId_토큰_없으면_empty_반환() {
        // given: TTL 만료 또는 deleteByUserId() 이후 null 반환 상황
        // null 반환 → Optional.ofNullable(null) → Optional.empty()
        when(valueOperations.get("refresh:1")).thenReturn(null);

        // when & then
        assertThat(refreshTokenService.findByUserId(1L)).isEmpty();
    }

    @Test
    void deleteByUserId_키_삭제_호출() {
        // when
        refreshTokenService.deleteByUserId(1L);

        // then
        // opsForValue() 없이 redisTemplate.delete()를 직접 호출하는지 검증
        verify(redisTemplate).delete("refresh:1");
    }
}