package com.concertticket.event;

import com.concertticket.common.exception.BusinessException;
import com.concertticket.common.exception.ErrorCode;
import com.concertticket.event.dto.CreateEventRequest;
import com.concertticket.event.dto.CreateScheduleRequest;
import com.concertticket.event.dto.EventResponse;
import com.concertticket.event.dto.ScheduleResponse;
import com.concertticket.venue.Venue;
import com.concertticket.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 공연 및 회차 비즈니스 로직 서비스. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final EventScheduleRepository eventScheduleRepository;

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        // 연관 엔티티 조회: 없으면 BusinessException(VENUE_NOT_FOUND)
        // GlobalExceptionHandler가 ErrorCode.httpStatus로 응답 코드를 결정
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));

        Event event = Event.create(
                venue,
                request.title(),
                request.artist(),
                request.genre(),
                request.description(),
                request.ticketOpenAt()
        );
        Event saved = eventRepository.save(event);
        // from() 내 venue.getName() 호출 — 같은 @Transactional 범위이므로 LAZY 로딩 안전
        return EventResponse.from(saved);
    }

    @Transactional
    public ScheduleResponse addSchedule(Long eventId, CreateScheduleRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        EventSchedule schedule = EventSchedule.create(
                event,
                request.startAt(),
                request.endAt(),
                request.totalSeatCount()
        );
        EventSchedule saved = eventScheduleRepository.save(schedule);
        return ScheduleResponse.from(saved);
    }
}