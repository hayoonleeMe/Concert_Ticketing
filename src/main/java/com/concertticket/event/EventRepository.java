package com.concertticket.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByVenueId(Long venueId);  // 연관관계 필드 탐색: venue.id로 WHERE 조건 자동 변환
}
