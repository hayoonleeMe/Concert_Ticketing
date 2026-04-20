package com.concertticket.venue;

import com.concertticket.auth.JwtAuthenticationFilter;
import com.concertticket.auth.handler.CustomAccessDeniedHandler;
import com.concertticket.auth.handler.CustomAuthenticationEntryPoint;
import com.concertticket.auth.handler.OAuth2SuccessHandler;
import com.concertticket.auth.oauth2.CustomOAuth2UserService;
import com.concertticket.config.SecurityConfig;
import com.concertticket.venue.dto.CreateVenueRequest;
import com.concertticket.venue.dto.VenueResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest: Controller + SecurityFilter + @ControllerAdvice만 로드 (Service/Repository 제외)
// SecurityConfig는 WebMvcTypeExcludeFilter에 의해 제외되므로 @Import로 명시 로드
// → 미로드 시 Spring Boot 기본 체인(anyRequest().authenticated())으로 폴백 — 역할 검사 무력화
@WebMvcTest(VenueController.class)
@Import(SecurityConfig.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트 대상 Service는 @MockitoBean으로 대체
    @MockitoBean
    private VenueService venueService;

    // SecurityConfig 의존 빈 — @WebMvcTest에서 Spring Context가 이 빈들을 요구함
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;
    @MockitoBean
    private CustomAccessDeniedHandler accessDeniedHandler;
    @MockitoBean
    private CustomOAuth2UserService oAuth2UserService;
    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @BeforeEach
    void setUp() throws Exception {
        // @MockitoBean으로 등록된 JwtAuthenticationFilter는 doFilter()도 stub됨
        // chain.doFilter()를 호출하지 않으면 요청이 DispatcherServlet에 도달하지 못해 Handler: null, 200 반환
        // → pass-through stub으로 필터 체인을 정상 진행시킴
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(
                any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));

        // accessDeniedHandler mock이 아무것도 하지 않으면 response가 commit되지 않아
        // DispatcherServlet이 그대로 실행되고 Controller의 201이 반환됨
        // → sendError(403)으로 response를 commit하여 DispatcherServlet 실행을 차단
        doAnswer(inv -> {
            HttpServletResponse res = inv.getArgument(1);
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }).when(accessDeniedHandler).handle(any(), any(), any());
    }

    @Test
    @DisplayName("ADMIN 권한으로 공연장 등록 성공 — 201 반환")
    // @WithMockUser: 실제 JWT 없이 SecurityContext에 인증된 사용자 삽입
    @WithMockUser(roles = "ADMIN")
    void create_adminSuccess() throws Exception {
        // given
        CreateVenueRequest request = new CreateVenueRequest("올림픽공원", "서울 송파구", 10000);
        VenueResponse mockResponse = new VenueResponse(1L, "올림픽공원", "서울 송파구", 10000);
        given(venueService.create(any(CreateVenueRequest.class))).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/admin/venues")
                        // csrf(): Spring Security CSRF 토큰 자동 포함 (테스트 환경 필수)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                // jsonPath: JSON 경로 기반 단언 — "$" = 루트, ".data.name" = data 필드의 name
                .andExpect(jsonPath("$.data.name").value("올림픽공원"))
                .andExpect(jsonPath("$.data.totalCapacity").value(10000));
    }

    @Test
    @DisplayName("USER 권한으로 Admin API 호출 — 403 반환")
    @WithMockUser(roles = "USER")
    void create_userForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/venues")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("올림픽공원", "서울 송파구", 10000))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("필수 필드 누락 — 400 반환")
    @WithMockUser(roles = "ADMIN")
    void create_validationFail_blankName() throws Exception {
        // name을 빈 문자열로 보내면 @NotBlank 위반
        CreateVenueRequest invalidRequest = new CreateVenueRequest("", "서울 송파구", 10000);

        mockMvc.perform(post("/api/admin/venues")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}