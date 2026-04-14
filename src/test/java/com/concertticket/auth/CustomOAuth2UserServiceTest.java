package com.concertticket.auth;

import com.concertticket.auth.oauth2.CustomOAuth2User;
import com.concertticket.auth.oauth2.CustomOAuth2UserService;
import com.concertticket.user.OAuthProvider;
import com.concertticket.user.User;
import com.concertticket.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    // @Spy: 실제 인스턴스를 생성하면서 fetchOAuth2User()만 대체해 실제 HTTP 호출을 방지
    @Spy
    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2UserRequest mockRequest(String registrationId) {
        // ClientRegistration과 OAuth2UserRequest는 내부 복잡도가 높아 mock 사용
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(clientRegistration.getRegistrationId()).thenReturn(registrationId);
        // userRequest.getClientRegistration().getRegistrationId() 체이닝 stub
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        return userRequest;
    }

    @Test
    void loadUser_신규_구글_사용자_DB에_저장() {
        // given
        // Google flat 구조: sub(고유 ID), email, name 최상위 필드
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google-uid-123");
        attributes.put("email", "test@gmail.com");
        attributes.put("name", "Test User");

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        OAuth2UserRequest userRequest = mockRequest("google");
        // @Spy: when().thenReturn() 대신 doReturn().when() 사용 — 실제 메서드 호출을 방지하기 위해
        doReturn(oAuth2User).when(customOAuth2UserService).fetchOAuth2User(userRequest);

        // 신규 사용자 — DB에 존재하지 않음
        when(userRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-uid-123"))
                .thenReturn(Optional.empty());
        User savedUser = mock(User.class);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        // loadUser()의 반환 타입이 CustomOAuth2User(Decorator)인지 확인
        assertThat(result).isInstanceOf(CustomOAuth2User.class);
        // 신규 가입이므로 save()가 1회 호출
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void loadUser_기존_카카오_사용자_이름_업데이트() {
        // given
        // Kakao nested 구조: { id, kakao_account: { email, profile: { nickname } } }
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "Updated Name");
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "kakao@test.com");
        kakaoAccount.put("profile", profile);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 456L);
        attributes.put("kakao_account", kakaoAccount);

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        OAuth2UserRequest userRequest = mockRequest("kakao");
        doReturn(oAuth2User).when(customOAuth2UserService).fetchOAuth2User(userRequest);

        // 기존 사용자 — DB에 이미 존재
        User existingUser = mock(User.class);
        when(userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, "456"))
                .thenReturn(Optional.of(existingUser));

        // when
        customOAuth2UserService.loadUser(userRequest);

        // then
        // 재로그인이므로 save()는 호출되지 않음 — 변경 감지(updateName)만 발생
        verify(userRepository, never()).save(any());
        // 소셜 프로필 닉네임이 updateName()으로 동기화되었는지 확인
        verify(existingUser).updateName("Updated Name");
    }

    @Test
    void loadUser_카카오_이메일_null_synthetic_email로_저장() {
        // given
        // 이메일 동의 거부 또는 전화번호 전용 계정 — kakao_account에 email 키 없음
        // → KakaoOAuth2UserInfo.getEmail() == null
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "No Email User");
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("profile", profile);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 789L);
        attributes.put("kakao_account", kakaoAccount);

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        OAuth2UserRequest userRequest = mockRequest("kakao");
        doReturn(oAuth2User).when(customOAuth2UserService).fetchOAuth2User(userRequest);

        // 신규 사용자 — DB에 존재하지 않음
        when(userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, "789"))
                .thenReturn(Optional.empty());

        // ArgumentCaptor로 save()에 전달된 User를 캡처해 email 값 검증
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(mock(User.class));

        // when
        customOAuth2UserService.loadUser(userRequest);

        // then
        // email null → synthetic email "kakao_789@oauth.placeholder"로 대체 확인
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("kakao_789@oauth.placeholder");
    }
}