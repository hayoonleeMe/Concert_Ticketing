package com.concertticket.user;

import com.concertticket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "users", // "user"는 MySQL 예약어; 반드시 "users"로 명시
        indexes = {
                @Index(name = "idx_user_provider", columnList = "provider, provider_id")    // 소셜 로그인 조회: findByProviderAndProviderId() 쿼리에 대응
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 프록시용; PROTECTED로 외부 new User() 차단
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT: DB가 id 채번 담당
    private Long id;

    @Column(nullable = false, unique = true, length = 100)  // unique: 이메일 중복 가입 방지; 복합 unique는 @Table(uniqueConstraints) 사용
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)  // STRING: "KAKAO" 문자열 저장; ORDINAL은 enum 순서 변경 시 데이터 오염 위험
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(length = 255)
    private String providerId;  // nullable: LOCAL 로그인은 providerId 없음

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")  // columnDefinition DEFAULT: DB 직접 INSERT 시에도 기본값 보장
    private UserRole role;

    public static User create(String email, String name, OAuthProvider provider, String providerId) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.provider = provider;
        user.providerId = providerId;
        user.role = UserRole.USER;  // 신규 가입자는 항상 USER; ADMIN 부여는 별도 프로세스
        return user;
    }
}