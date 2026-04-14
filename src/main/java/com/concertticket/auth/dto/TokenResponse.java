package com.concertticket.auth.dto;

// 토큰 재발급 응답 DTO
// accessToken: 30분 유효, Authorization 헤더에 사용
// refreshToken: 7일 유효, /api/auth/refresh 호출 시 사용
public record TokenResponse(String accessToken, String refreshToken) {}