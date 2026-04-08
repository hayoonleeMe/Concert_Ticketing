package com.concertticket.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);  // Spring Data가 WHERE u.email = ? 자동 생성
    Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);  // 소셜 로그인 재방문 시 기존 계정 조회; idx_user_provider 인덱스 활용
}
