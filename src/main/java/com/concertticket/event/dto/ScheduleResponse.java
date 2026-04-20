package com.concertticket.event.dto;

import com.concertticket.event.EventSchedule;

import java.time.LocalDateTime;

/**
 * 공연 회차 API 응답 DTO.
 */
public record ScheduleResponse(
        Long id,
        Long eventId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer totalSeatCount,
        // remainingSeatCount: Phase 8 Redis 캐싱 도입 전까지는 DB에서 직접 읽는 값
        Integer remainingSeatCount
) {
    public static ScheduleResponse from(EventSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getEvent().getId(),
                schedule.getStartAt(),
                schedule.getEndAt(),
                schedule.getTotalSeatCount(),
                schedule.getRemainingSeatCount()
        );
    }
}