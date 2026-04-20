package com.concertticket.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * 공연 회차 등록 요청 DTO.
 */
public record CreateScheduleRequest(
        @NotNull(message = "시작 일시는 필수입니다")
        LocalDateTime startAt,

        @NotNull(message = "종료 일시는 필수입니다")
        LocalDateTime endAt,

        @NotNull(message = "총 좌석 수는 필수입니다")
        @Positive(message = "총 좌석 수는 양수여야 합니다")
        Integer totalSeatCount
) {}