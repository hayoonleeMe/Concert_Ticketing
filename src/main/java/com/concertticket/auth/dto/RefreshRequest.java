package com.concertticket.auth.dto;

import jakarta.validation.constraints.NotBlank;

// @NotBlank: null, 빈 문자열, 공백 문자열 모두 차단
public record RefreshRequest(
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        String refreshToken
) {}