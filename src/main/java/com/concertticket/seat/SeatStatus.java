package com.concertticket.seat;

public enum SeatStatus {

    AVAILABLE,  // 예약 가능; Seat.create() 팩토리에서 초기값으로 고정
    RESERVED,   // 임시 선점 (결제 대기 중); 결제 완료 시 SOLD로 전환
    SOLD        // 결제 완료
}