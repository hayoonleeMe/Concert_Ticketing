package com.concertticket.auth.oauth2;

// provider별 속성 맵에서 사용자 정보를 추출하는 Strategy 인터페이스
// Google(flat 구조)과 Kakao(nested 구조)의 차이를 이 인터페이스 뒤에 숨긴다
public interface OAuth2UserInfo {

    // 소셜 provider의 사용자 고유 식별자 (DB의 provider_id 컬럼에 저장)
    String getProviderId();

    // 사용자 이메일 (User.email 컬럼에 저장)
    String getEmail();

    // 사용자 표시 이름 (User.name 컬럼에 저장, 재로그인 시 동기화)
    String getName();
}