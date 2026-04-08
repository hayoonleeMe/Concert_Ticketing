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
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),  // 404: orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))

    // 공연장 (V)
    VENUE_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "공연장을 찾을 수 없습니다."),  // 404: VenueService, EventService 양쪽에서 사용

    // 이벤트 (E)
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다."),  // 404
    EVENT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "E002", "이벤트 회차를 찾을 수 없습니다."),  // 404: Event와 별개 조회 실패 코드

    // 좌석 (S)
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "좌석을 찾을 수 없습니다."),  // 404
    SEAT_ALREADY_TAKEN(HttpStatus.CONFLICT, "S002", "이미 예약된 좌석입니다."),  // 409: 낙관적 락 충돌 후 재시도 한도 초과 시 이 코드로 응답
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "S003", "예약 불가능한 좌석입니다.");  // 409: seat.reserve() 또는 decreaseRemainingSeatCount()에서 상태 불일치

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
