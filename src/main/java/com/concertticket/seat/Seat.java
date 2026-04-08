package com.concertticket.seat;

import com.concertticket.common.entity.BaseEntity;
import com.concertticket.common.exception.BusinessException;
import com.concertticket.common.exception.ErrorCode;
import com.concertticket.event.EventSchedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "seats",
        indexes = {
                @Index(name = "idx_seat_schedule_status", columnList = "event_schedule_id, status")  // 복합 인덱스: event_schedule_id(선행) → 특정 회차의 AVAILABLE 좌석 조회에 최적
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY: 좌석 단건 조회 시 EventSchedule까지 JOIN하지 않음
    @JoinColumn(name = "event_schedule_id", nullable = false)
    private EventSchedule eventSchedule;

    @Column(nullable = false, length = 20)
    private String seatNumber;

    @Column(nullable = false, precision = 10, scale = 2)  // DECIMAL(10,2): float/double의 부동소수점 오차 없이 금액 정확 저장
    private BigDecimal price;

    @Enumerated(EnumType.STRING)  // STRING: "AVAILABLE" 문자열 저장; ORDINAL은 enum 순서 변경 시 데이터 오염
    @Column(nullable = false, length = 20)
    private SeatStatus status;

    @Version  // 낙관적 락: UPDATE 시 WHERE version=현재값 자동 추가; 불일치 시 OptimisticLockException
    @Column(nullable = false)  // NOT NULL: DDL의 DEFAULT 0과 반드시 동기화
    private Long version;

    public static Seat create(EventSchedule eventSchedule, String seatNumber, BigDecimal price) {
        Seat seat = new Seat();
        seat.eventSchedule = eventSchedule;
        seat.seatNumber = seatNumber;
        seat.price = price;
        seat.status = SeatStatus.AVAILABLE;  // 생성 시 항상 AVAILABLE; 외부에서 status 주입 불가로 불변 초기 상태 보장
        return seat;
    }

    public void reserve() {
        if (this.status != SeatStatus.AVAILABLE) {  // AVAILABLE이 아니면 예약 불가; 낙관적 락 충돌은 Hibernate가 별도 처리
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        this.status = SeatStatus.RESERVED;  // @Setter 없이 같은 클래스 내부에서만 this.필드 직접 수정 가능
    }
}