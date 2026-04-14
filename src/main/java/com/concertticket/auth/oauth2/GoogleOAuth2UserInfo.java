package com.concertticket.auth.oauth2;

import lombok.RequiredArgsConstructor;
import java.util.Map;

// Google userinfo 응답 구조 (flat):
// { "sub": "...", "email": "...", "name": "...", "picture": "..." }
@RequiredArgsConstructor
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        // sub: Google의 사용자 고유 ID — 이메일은 변경 가능하므로 sub를 식별자로 사용
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}