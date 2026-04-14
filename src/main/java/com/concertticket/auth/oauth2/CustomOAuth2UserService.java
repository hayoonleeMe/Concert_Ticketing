package com.concertticket.auth.oauth2;

import com.concertticket.user.OAuthProvider;
import com.concertticket.user.User;
import com.concertticket.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    // User.updateName() 변경 감지 + User.create() save()를 하나의 트랜잭션으로
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스가 provider의 userinfo endpoint를 HTTP 호출해 속성 맵 반환
        OAuth2User oAuth2User = fetchOAuth2User(userRequest);

        // application-dev.yml의 registration 키 ("google", "kakao")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // provider별 속성 추출 전략 선택 (Google: flat / Kakao: nested)
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());

        // registrationId를 대문자로 변환해 OAuthProvider enum과 매핑
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        // 기존 회원 여부 확인 후 upsert
        User user = userRepository
                .findByProviderAndProviderId(provider, userInfo.getProviderId())
                .map(existingUser -> {
                    // 재로그인: 소셜 프로필 변경분(닉네임 등) 동기화
                    existingUser.updateName(userInfo.getName());
                    return existingUser;
                })
                .orElseGet(() -> {
                    // 최초 로그인: 신규 회원 저장
                    // 카카오 account_email은 선택 동의 → 거부 시, 또는 전화번호 전용 계정이면 null
                    // User.email은 NOT NULL 컬럼이므로 null이면 synthetic email로 대체
                    String email = userInfo.getEmail() != null
                            ? userInfo.getEmail()
                            : registrationId + "_" + userInfo.getProviderId() + "@oauth.placeholder";
                    return userRepository.save(
                            User.create(email, userInfo.getName(), provider, userInfo.getProviderId())
                    );
                });

        // CustomOAuth2User로 래핑: SuccessHandler에서 User 엔티티 직접 참조 가능
        return new CustomOAuth2User(oAuth2User, user);
    }

    // super.loadUser() 호출을 분리 → 테스트에서 @Spy로 대체 가능 (실제 HTTP 호출 방지)
    public OAuth2User fetchOAuth2User(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }
}