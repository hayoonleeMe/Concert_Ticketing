package com.concertticket.seat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * 좌석 일괄 등록 요청 DTO.
 * 중첩 record SeatItem에 @Valid를 적용해 각 항목도 Bean Validation이 실행됩니다.
 */
public record CreateSeatsRequest(
        // @NotEmpty: null과 빈 리스트 모두 거부
        // @Valid: 리스트 각 요소(SeatItem) 내부 필드 검증도 함께 수행
        @NotEmpty(message = "좌석 목록은 비어있을 수 없습니다")
        List<@Valid SeatItem> seats
) {
    /**
     * 개별 좌석 등록 항목.
     * 중첩 record로 선언해 CreateSeatsRequest와 응집도를 높이고 별도 파일 없이 함께 관리한다.
     */
    public record SeatItem(
            @NotBlank(message = "좌석 번호는 필수입니다")
            String seatNumber,

            @NotNull(message = "가격은 필수입니다")
            @Positive(message = "가격은 양수여야 합니다")
            BigDecimal price
    ) {}
}