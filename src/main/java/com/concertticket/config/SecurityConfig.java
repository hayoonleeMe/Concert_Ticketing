package com.concertticket.config;

import com.concertticket.auth.JwtAuthenticationFilter;
import com.concertticket.auth.handler.CustomAccessDeniedHandler;
import com.concertticket.auth.handler.CustomAuthenticationEntryPoint;
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
@EnableMethodSecurity  // @PreAuthorize("hasRole('ADMIN')") 활성화 (Phase 5에서 사용)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // JWT 방식: CSRF 토큰 불필요
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        // 서버에 세션 저장 안 함
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공연 조회: 비로그인도 가능
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        // 헬스체크: CI/CD 프로브용
                        .requestMatchers("/actuator/health").permitAll()
                        // 그 외 전부 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // 401: 토큰 없음/만료
                        .authenticationEntryPoint(authenticationEntryPoint)
                        // 403: 권한 부족
                        .accessDeniedHandler(accessDeniedHandler)
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
