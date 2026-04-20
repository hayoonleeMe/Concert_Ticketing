package com.concertticket.config;

import com.concertticket.auth.JwtAuthenticationFilter;
import com.concertticket.auth.handler.CustomAccessDeniedHandler;
import com.concertticket.auth.handler.CustomAuthenticationEntryPoint;
import com.concertticket.auth.handler.OAuth2SuccessHandler;
import com.concertticket.auth.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
// @PreAuthorize("hasRole('ADMIN')") 활성화 (Phase 5에서 사용)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // JWT 방식: CSRF 토큰 불필요
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        // IF_REQUIRED: OAuth2 Authorization Code Flow에서 state 파라미터를
                        // 임시 저장하는 세션이 필요 — STATELESS로 설정하면 OAuth2 흐름이 깨짐
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // 공연 조회: 비로그인도 가능
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        // 헬스체크: CI/CD 프로브용
                        .requestMatchers("/actuator/health").permitAll()
                        // Refresh Token 재발급: 만료된 Access Token으로 접근하므로 비인증 허용
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        // 추가: Admin API는 ADMIN 역할 필수 — @PreAuthorize와 이중 방어 구조
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 그 외 전부 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // 401: 토큰 없음/만료
                        .authenticationEntryPoint(authenticationEntryPoint)
                        // 403: 권한 부족
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                // DefaultOAuth2UserService 대신 커스텀 구현체: User upsert 처리
                                .userService(oAuth2UserService)
                        )
                        // 인증 성공 후 JWT 발급 + 프론트엔드 리다이렉트
                        .successHandler(oAuth2SuccessHandler)
                )
                // JWT 필터를 표준 인증 필터 앞에 배치
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Phase 4 OAuth2에서 사용, 미리 빈 등록
        return new BCryptPasswordEncoder();
    }
}
