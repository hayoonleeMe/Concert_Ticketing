package com.concertticket.seat;

import com.concertticket.common.exception.BusinessException;
import com.concertticket.common.exception.ErrorCode;
import com.concertticket.event.EventSchedule;
import com.concertticket.event.EventScheduleRepository;
import com.concertticket.seat.dto.CreateSeatsRequest;
import com.concertticket.seat.dto.SeatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 좌석 비즈니스 로직 서비스. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventScheduleRepository eventScheduleRepository;

    @Transactional
    public List<SeatResponse> bulkCreate(Long scheduleId, CreateSeatsRequest request) {
        EventSchedule schedule = eventScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_SCHEDULE_NOT_FOUND));

        // Stream으로 각 SeatItem을 Seat 엔티티로 변환
        List<Seat> seats = request.seats().stream()
                .map(item -> Seat.create(schedule, item.seatNumber(), item.price()))
                .toList();  // Java 16+ 불변 리스트 반환

        // saveAll: 단일 Flush 컨텍스트에서 N개 INSERT
        List<Seat> saved = seatRepository.saveAll(seats);
        return saved.stream()
                .map(SeatResponse::from)
                .toList();
    }
}