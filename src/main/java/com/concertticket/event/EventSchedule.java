package com.concertticket.event;

import com.concertticket.common.entity.BaseEntity;
import com.concertticket.common.exception.BusinessException;
import com.concertticket.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY: EventSchedule 목록 조회 시 각 Event를 즉시 로드하지 않음
    @JoinColumn(name = "event_id", nullable = false)  // event_id: DDL의 fk_schedule_event 외래키 컬럼과 이름 일치
    private Event event;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Integer totalSeatCount;  // 변하지 않는 총 좌석 수

    @Column(nullable = false)
    private Integer remainingSeatCount;  // 별도 카운터: COUNT(*) 없이 잔여석 O(1) 조회 가능

    public static EventSchedule create(Event event, LocalDateTime startAt,
                                       LocalDateTime endAt, Integer totalSeatCount) {
        EventSchedule schedule = new EventSchedule();
        schedule.event = event;
        schedule.startAt = startAt;
        schedule.endAt = endAt;
        schedule.totalSeatCount = totalSeatCount;
        schedule.remainingSeatCount = totalSeatCount;  // 초기값 = 총 좌석 수
        return schedule;
    }

    public void decreaseRemainingSeatCount() {
        if (this.remainingSeatCount <= 0) {  // 도메인 규칙을 엔티티에 캡슐화: 서비스 계층이 중복 구현하지 않아도 됨
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);  // S003: 잔여석 없음; S002(ALREADY_TAKEN)는 낙관적 락 충돌에 사용
        }
        this.remainingSeatCount--;  // 동시성 보호는 Seat.@Version 담당; 여기서는 단순 카운터 감소
    }
}