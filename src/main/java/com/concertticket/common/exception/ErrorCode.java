package com.concertticket.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 (C)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),

    // 유저 (U)
    // 404: orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),

    // 공연장 (V)
    // 404: VenueService, EventService 양쪽에서 사용
    VENUE_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "공연장을 찾을 수 없습니다."),

    // 이벤트 (E)
    // 404
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다."),
    // 404: Event와 별개 조회 실패 코드
    EVENT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "E002", "이벤트 회차를 찾을 수 없습니다."),

    // 좌석 (S)
    // 404
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "좌석을 찾을 수 없습니다."),
    // 409: 낙관적 락 충돌 후 재시도 한도 초과 시 이 코드로 응답
    SEAT_ALREADY_TAKEN(HttpStatus.CONFLICT, "S002", "이미 예약된 좌석입니다."),
    // 409: seat.reserve() 또는 decreaseRemainingSeatCount()에서 상태 불일치
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "S003", "예약 불가능한 좌석입니다."),

    // 인증/인가 (A)
    // 401: 토큰 없이 보호된 엔드포인트 접근 시
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    // 403: 권한 부족 (예: USER가 ADMIN 전용 API 호출)
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    // 401: 서명 불일치, 잘못된 형식
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 토큰입니다."),
    // 401: exp 클레임 초과
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "만료된 토큰입니다."),

    // A005: Refresh Token 서명 불일치, 형식 오류, 또는 Redis 저장 토큰과 불일치
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A005", "유효하지 않은 리프레시 토큰입니다."),
    // A006: Refresh Token의 exp 클레임 초과
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A006", "만료된 리프레시 토큰입니다."),
    // A007: google/kakao 외 provider로 시도한 경우
    UNSUPPORTED_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "A007", "지원하지 않는 소셜 로그인 제공자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
