package com.concertticket.auth;

import com.concertticket.user.UserRole;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder    // builder 패턴
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private UserRole role;

    public static UserPrincipal fromClaims(Claims claims) {
        // DB 조회 없이 JWT 클레임만으로 Principal 생성
        // 매 요청마다 SELECT 없이 인증 처리 → 수평 확장(서버 추가) 시 성능 이점
        return UserPrincipal.builder()
                // sub → userId
                .id(Long.parseLong(claims.getSubject()))
                .email(claims.get("email", String.class))
                // String → enum 변환
                .role(UserRole.valueOf(claims.get("role", String.class)))
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_" prefix: Spring Security 컨벤션
        // hasRole('USER') → 내부적으로 "ROLE_USER" 검사
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        // OAuth2 사용자는 비밀번호 없음
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }
}
