package com.concertticket.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 유효한 토큰: SecurityContext에 인증 정보 저장
            setAuthentication(token);
        }
        // 토큰 없거나 검증 실패: SecurityContext 비어있음 → EntryPoint가 401 처리

        // 반드시 다음 필터로 진행
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // "Bearer " 접두사 포함 여부 확인 후 토큰 부분만 추출
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer "(7글자) 이후부터 추출
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setAuthentication(String token) {
        Claims claims = jwtTokenProvider.getClaims(token);
        // DB 조회 없이 claims에서 생성
        UserPrincipal principal = UserPrincipal.fromClaims(claims);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        // credentials: 인증 완료 후 null (보안: 자격증명 메모리 제거)
                        null,
                        // 세 번째 파라미터 있음 = authenticated=true 처리
                        principal.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
