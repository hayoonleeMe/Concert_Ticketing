package com.concertticket.user;

public enum OAuthProvider {
    KAKAO,
    GOOGLE,
    LOCAL  // 이메일/비밀번호 로그인용; 소셜 로그인이 아닌 일반 회원가입
}