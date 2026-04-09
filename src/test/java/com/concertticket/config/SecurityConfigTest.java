package com.concertticket.config;

import com.concertticket.auth.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 전체 Spring 컨텍스트 로드 (Security 필터 체인 포함)
@SpringBootTest
// MockMvc 자동 설정 — 실제 필터 체인을 거치는 Mock HTTP 요청 가능
@AutoConfigureMockMvc
// src/test/resources/application-test.yml 활성화 → H2 인메모리 DB 사용
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Spring Boot 3.4+: @MockBean 제거됨, @MockitoBean으로 교체
    // JwtTokenProvider 빈을 Mock으로 등록 — 컨텍스트 로드 성공 + 실제 JWT 파싱 불필요
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("actuator/health — 인증 없이 200 응답")
    void actuatorHealth_인증없이접근가능() throws Exception {
        // SecurityConfig: .requestMatchers("/actuator/health").permitAll()
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/events/** — 인증 없이 접근 가능")
    void events_GET_인증없이접근가능() throws Exception {
        // SecurityConfig: .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
        // not(401): 401이 아니면 통과. Hamcrest not() — AssertJ에는 isNotEqualTo(int)가 없음
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("보호된 엔드포인트 — 토큰 없으면 401 JSON (HTML redirect 아님)")
    void 보호된엔드포인트_토큰없음_401JSON() throws Exception {
        // SecurityConfig: .anyRequest().authenticated() → 토큰 없으면 401
        // CustomAuthenticationEntryPoint가 HTML redirect 대신 JSON 응답 반환하는지 검증
        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }
}