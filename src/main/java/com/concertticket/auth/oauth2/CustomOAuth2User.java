package com.concertticket.auth.oauth2;

import com.concertticket.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.Map;

// DefaultOAuth2UserService가 반환한 OAuth2User를 래핑해 DB User 엔티티를 함께 전달
// OAuth2SuccessHandler에서 userId/role을 얻기 위한 2차 DB 조회를 방지한다
@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    // DefaultOAuth2UserService.loadUser()가 반환한 원본 OAuth2User
    private final OAuth2User delegate;

    // CustomOAuth2UserService에서 upsert된 User 엔티티
    private final User user;

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        // OAuth2User 계약: provider가 부여한 principal name 반환
        return delegate.getName();
    }
}