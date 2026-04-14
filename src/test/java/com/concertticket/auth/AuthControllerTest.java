package com.concertticket.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.concertticket.auth.dto.RefreshRequest;
import com.concertticket.user.User;
import com.concertticket.user.UserRepository;
import com.concertticket.user.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 전체 Spring 컨텍스트 로드 (Security 필터 체인 포함)
@SpringBootTest
// 실제 Security 필터 체인을 거치는 MockMvc 자동 설정
@AutoConfigureMockMvc
// H2 인메모리 DB 사용 (MySQL/Docker 불필요)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // @SpringBootTest 컨텍스트가 실제 Redis 자동 설정을 시도하므로 mock으로 대체
    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void refresh_유효한_토큰_200_새_토큰_반환() throws Exception {
        // given
        String refreshToken = "valid-refresh-token";
        Long userId = 1L;

        // User 엔티티 mock — @NoArgsConstructor(access = PROTECTED)이므로 직접 생성 불가
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getRole()).thenReturn(UserRole.USER);

        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn(userId);
        // 서명 유효 + Redis에 동일 토큰 존재 → 정상 흐름
        when(refreshTokenService.findByUserId(userId)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(userId, "test@example.com", UserRole.USER))
                .thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(userId)).thenReturn("new-refresh-token");

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                // ApiResponse.ok(TokenResponse) → { "success": true, "data": { "accessToken": ..., "refreshToken": ... } }
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));

        // Token Rotation 확인 — 새 Refresh Token으로 save() 호출
        verify(refreshTokenService).save(userId, "new-refresh-token");
    }

    @Test
    void refresh_만료된_토큰_401_A006() throws Exception {
        // given
        // ExpiredJwtException: JJWT가 exp 클레임 초과 시 던지는 예외
        when(jwtTokenProvider.getUserId(anyString()))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        // when & then
        // AuthController: catch(ExpiredJwtException) → BusinessException(EXPIRED_REFRESH_TOKEN) → 401
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("expired-token"))))
                .andExpect(status().isUnauthorized())
                // ApiResponse.fail(errorCode.getMessage()) → { "success": false, "message": "만료된 리프레시 토큰입니다." }
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("만료된 리프레시 토큰입니다."));
    }

    @Test
    void refresh_서명_불일치_토큰_401_A005() throws Exception {
        // given
        // JwtException: 서명 불일치, 형식 오류 시 JJWT가 던지는 기반 예외
        when(jwtTokenProvider.getUserId(anyString()))
                .thenThrow(new JwtException("invalid signature"));

        // when & then
        // AuthController: catch(JwtException) → BusinessException(INVALID_REFRESH_TOKEN) → 401
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("tampered-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
    }

    @Test
    void refresh_Redis에_없는_토큰_401_A005() throws Exception {
        // given: 서명은 유효하지만 Redis에 저장된 토큰 없음 (로그아웃 후 재사용 시도)
        when(jwtTokenProvider.getUserId("valid-but-evicted-token")).thenReturn(1L);
        // Optional.empty() → orElseThrow(INVALID_REFRESH_TOKEN) → 401
        when(refreshTokenService.findByUserId(1L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("valid-but-evicted-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
    }

    @Test
    void refresh_빈_토큰값_400() throws Exception {
        // when & then
        // @NotBlank 검증 실패 → MethodArgumentNotValidException → GlobalExceptionHandler → 400
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(""))))
                .andExpect(status().isBadRequest())
                // RefreshRequest @NotBlank 메시지 확인
                .andExpect(jsonPath("$.message").value("리프레시 토큰은 필수입니다."));
    }
}