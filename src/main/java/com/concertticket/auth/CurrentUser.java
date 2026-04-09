package com.concertticket.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)      // 메서드 파라미터에만 적용
@Retention(RetentionPolicy.RUNTIME) // 런타임에 어노테이션 정보 유지 (Spring이 리플렉션으로 읽음)
@Documented                         // Javadoc에 포함
@AuthenticationPrincipal            // Spring Security: SecurityContext → Principal 자동 주입
public @interface CurrentUser {
}
