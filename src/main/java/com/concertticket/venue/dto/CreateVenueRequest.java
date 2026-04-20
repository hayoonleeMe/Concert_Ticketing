package com.concertticket.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 공연장 등록 요청 DTO.
 * Controller에서 @Valid로 검증된 후 VenueService.create()에 전달됩니다.
 */
public record CreateVenueRequest(
        // @NotBlank: null과 공백 문자열 모두 거부 (@NotNull만으로는 "" 통과됨)
        @NotBlank(message = "공연장 이름은 필수입니다")
        String name,

        @NotBlank(message = "주소는 필수입니다")
        String address,

        // @Positive: 0 포함 비양수 거부 (좌석 수는 반드시 1 이상)
        @NotNull(message = "총 좌석 수는 필수입니다")
        @Positive(message = "총 좌석 수는 양수여야 합니다")
        Integer totalCapacity
) {}