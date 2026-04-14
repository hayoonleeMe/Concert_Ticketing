package com.concertticket.auth;

import com.concertticket.auth.dto.RefreshRequest;
import com.concertticket.auth.dto.TokenResponse;
import com.concertticket.common.exception.BusinessException;
import com.concertticket.common.exception.ErrorCode;
import com.concertticket.common.response.ApiResponse;
import com.concertticket.user.User;
import com.concertticket.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {

        // 서명 검증 + 만료 여부 확인 — validateToken() 대신 직접 파싱해 예외 타입으로 분기
        String token = request.refreshToken();
        Long userId;
        try {
            userId = jwtTokenProvider.getUserId(token);
        } catch (ExpiredJwtException e) {
            // exp 클레임 초과: A006
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 형식 오류: A005
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Redis에 저장된 토큰과 일치 여부 확인
        String storedToken = refreshTokenService.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (!storedToken.equals(token)) {
            // 이미 교체된 토큰 재사용 시도 → 잠재적 탈취 신호
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새 Access Token + Refresh Token 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Token Rotation: 이전 Refresh Token 폐기 후 새 토큰 저장
        refreshTokenService.save(user.getId(), newRefreshToken);

        return ResponseEntity.ok(ApiResponse.ok(new TokenResponse(newAccessToken, newRefreshToken)));
    }
}