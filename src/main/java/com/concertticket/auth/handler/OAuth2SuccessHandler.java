package com.concertticket.auth.handler;

import com.concertticket.auth.JwtTokenProvider;
import com.concertticket.auth.RefreshTokenService;
import com.concertticket.auth.oauth2.CustomOAuth2User;
import com.concertticket.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${oauth2.redirect-url}")
    private String redirectUrl;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // CustomOAuth2UserService.loadUser()가 반환한 CustomOAuth2User
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // User 엔티티 직접 참조 — 2차 DB 조회 없음
        User user = oAuth2User.getUser();

        // Access Token (30분) + Refresh Token (7일) 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Refresh Token Redis 저장 (재로그인 시 이전 토큰 덮어씀 — Token Rotation)
        refreshTokenService.save(user.getId(), refreshToken);

        // 프론트엔드로 토큰 전달 (쿼리 파라미터 방식)
        // 프로덕션 권장: HttpOnly 쿠키 + SameSite=Strict (XSS 방어)
        response.sendRedirect(redirectUrl + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken);
    }
}