package com.concertticket.auth.oauth2;

import com.concertticket.common.exception.BusinessException;
import com.concertticket.common.exception.ErrorCode;
import java.util.Map;

// registrationId(provider 이름)로 적합한 OAuth2UserInfo 구현체를 생성하는 정적 팩토리
// CustomOAuth2UserService에서 분기 로직을 제거해 단일 책임 원칙을 지킨다
public class OAuth2UserInfoFactory {

    // 유틸리티 클래스: 인스턴스화 방지
    private OAuth2UserInfoFactory() {}

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        // registrationId: application-dev.yml registration 키와 일치 (소문자)
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            // google/kakao 외 provider 시도 시 400 응답
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
        };
    }
}