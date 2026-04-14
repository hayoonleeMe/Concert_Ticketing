package com.concertticket.auth.oauth2;

import lombok.RequiredArgsConstructor;
import java.util.Map;

// 카카오 userinfo 응답 구조 (nested):
// { "id": 123456, "kakao_account": { "email": "...", "profile": { "nickname": "..." } } }
@RequiredArgsConstructor
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        // id: 최상위 숫자 필드 — 카카오 사용자 고유 식별자 (Long → String 변환)
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        // 이메일은 kakao_account 하위에 존재 (scope: account_email 동의 필요)
        Map<?, ?> kakaoAccount = (Map<?, ?>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        // 닉네임은 kakao_account.profile 하위에 존재 (scope: profile_nickname 동의 필요)
        Map<?, ?> kakaoAccount = (Map<?, ?>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        Map<?, ?> profile = (Map<?, ?>) kakaoAccount.get("profile");
        if (profile == null) {
            return null;
        }
        return (String) profile.get("nickname");
    }
}