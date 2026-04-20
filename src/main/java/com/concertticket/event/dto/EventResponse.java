package com.concertticket.event.dto;

import com.concertticket.event.Event;

import java.time.LocalDateTime;

/**
 * 공연 API 응답 DTO.
 * from() 정적 팩토리 메서드로 Entity → DTO 변환을 캡슐화합니다.
 */
public record EventResponse(
        Long id,
        Long venueId,
        // venueName 포함: 클라이언트가 공연장명 표시를 위해 추가 요청을 보내지 않아도 됨
        String venueName,
        String title,
        String artist,
        String genre,
        String description,
        LocalDateTime ticketOpenAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                // LAZY 로딩이지만 @Transactional 범위 내에서 호출되므로 안전
                event.getVenue().getId(),
                event.getVenue().getName(),
                event.getTitle(),
                event.getArtist(),
                event.getGenre(),
                event.getDescription(),
                event.getTicketOpenAt()
        );
    }
}
