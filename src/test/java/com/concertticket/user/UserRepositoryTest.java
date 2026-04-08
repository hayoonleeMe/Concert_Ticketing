package com.concertticket.user;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("저장된 사용자를 이메일로 조회할 수 있다")
    void findByEmail_존재하는_이메일() {
        userRepository.save(
                User.create("test@example.com", "테스터", OAuthProvider.KAKAO, "kakao-123"));

        Optional<User> result = userRepository.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일 조회 시 Optional.empty()를 반환한다")
    void findByEmail_존재하지_않는_이메일() {
        Optional<User> result = userRepository.findByEmail("nobody@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("provider와 providerId로 소셜 로그인 사용자를 조회할 수 있다")
    void findByProviderAndProviderId_카카오_사용자() {
        userRepository.save(
                User.create("kakao@example.com", "카카오유저", OAuthProvider.KAKAO, "kakao-456"));

        Optional<User> result = userRepository.findByProviderAndProviderId(
                OAuthProvider.KAKAO, "kakao-456");

        assertThat(result).isPresent();
        assertThat(result.get().getProvider()).isEqualTo(OAuthProvider.KAKAO);
    }
}