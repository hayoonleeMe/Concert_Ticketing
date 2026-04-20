package com.concertticket.venue;

import com.concertticket.venue.dto.CreateVenueRequest;
import com.concertticket.venue.dto.VenueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 공연장 비즈니스 로직 서비스. */
@Service
@RequiredArgsConstructor
// 클래스 레벨: 모든 메서드의 기본 트랜잭션을 readOnly = true로 설정
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;

    // 쓰기 메서드: 클래스 레벨 readOnly = true를 readOnly = false로 오버라이드
    @Transactional
    public VenueResponse create(CreateVenueRequest request) {
        // 도메인 팩토리 메서드: 엔티티 생성자(PROTECTED)를 우회하는 유일한 진입점
        Venue venue = Venue.create(request.name(), request.address(), request.totalCapacity());
        Venue saved = venueRepository.save(venue);
        return VenueResponse.from(saved);
    }
}