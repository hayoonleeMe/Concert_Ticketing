package com.concertticket.venue.dto;

import com.concertticket.venue.Venue;

/**
 * 공연장 API 응답 DTO.
 * from() 정적 팩토리 메서드로 Entity → DTO 변환을 캡슐화합니다.
 */
public record VenueResponse(
        Long id,
        String name,
        String address,
        Integer totalCapacity
) {
    // 팩토리 메서드: Service가 엔티티 필드를 직접 참조하지 않도록 캡슐화
    // 엔티티 구조가 바뀌어도 Service 코드를 수정하지 않고 from()만 수정하면 됨
    public static VenueResponse from(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddress(),
                venue.getTotalCapacity()
        );
    }
}