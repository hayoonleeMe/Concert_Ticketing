package com.concertticket.event.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 공연 등록 요청 DTO.
 */
public record CreateEventRequest(
        @NotNull(message = "공연장 ID는 필수입니다")
        Long venueId,

        @NotBlank(message = "공연 제목은 필수입니다")
        String title,

        @NotBlank(message = "아티스트명은 필수입니다")
        String artist,

        // genre, description은 선택 필드 — null 허용
        String genre,

        String description,

        // @FutureOrPresent: 과거 시각으로 티켓 오픈을 등록하는 실수 방지
        @NotNull(message = "티켓 오픈 일시는 필수입니다")
        @FutureOrPresent(message = "티켓 오픈 일시는 현재 또는 미래여야 합니다")
        LocalDateTime ticketOpenAt
) {}