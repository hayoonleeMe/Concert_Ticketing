package com.concertticket.auth.handler;

import com.concertticket.common.exception.ErrorCode;
import com.concertticket.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Spring이 자동 등록하는 Jackson ObjectMapper 빈 주입
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 기본 동작(HTML 302 redirect) 대신 JSON 401 응답
        // REST 클라이언트(앱, SPA)는 HTML redirect 처리 불가

        // 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 한글 메시지 깨짐 방지
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                // 프로젝트 표준 응답 포맷
                ApiResponse.fail(ErrorCode.UNAUTHORIZED.getMessage())
        );
    }
}
