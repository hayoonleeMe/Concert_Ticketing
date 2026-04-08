package com.concertticket.venue;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    // 기본 CRUD(save, findById, findAll, deleteById 등)는 JpaRepository가 제공한다.
    // Phase 2에서는 추가 메서드 없음.
}
