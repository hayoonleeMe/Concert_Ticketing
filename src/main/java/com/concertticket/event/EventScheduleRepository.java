package com.concertticket.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {

    List<EventSchedule> findByEventIdOrderByStartAtAsc(Long eventId);  // OrderBy...Asc: 메서드 이름으로 ORDER BY start_at ASC 자동 생성
}
