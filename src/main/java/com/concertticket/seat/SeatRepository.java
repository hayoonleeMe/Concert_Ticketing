package com.concertticket.seat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByEventScheduleIdAndStatus(Long eventScheduleId, SeatStatus status);  // idx_seat_schedule_status 인덱스를 타는 조건

    @Query("SELECT s FROM Seat s JOIN FETCH s.eventSchedule es JOIN FETCH es.event WHERE s.id = :id")  // JOIN FETCH: Seat + EventSchedule + Event 단 1개 쿼리; LAZY의 N+1 방지
    Optional<Seat> findByIdWithScheduleAndEvent(@Param("id") Long id);
}
