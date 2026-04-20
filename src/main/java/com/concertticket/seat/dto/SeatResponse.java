package com.concertticket.seat.dto;

import com.concertticket.seat.Seat;
import com.concertticket.seat.SeatStatus;

import java.math.BigDecimal;

/**
 * 좌석 API 응답 DTO.
 */
public record SeatResponse(
        Long id,
        String seatNumber,
        BigDecimal price,
        // SeatStatus enum: Jackson이 "AVAILABLE", "RESERVED", "SOLD" 문자열로 직렬화
        SeatStatus status
) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getPrice(),
                seat.getStatus()
        );
    }
}