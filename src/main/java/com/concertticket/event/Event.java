package com.concertticket.event;

import com.concertticket.common.entity.BaseEntity;
import com.concertticket.venue.Venue;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY: Event 조회 시 Venue를 즉시 JOIN하지 않음; 필요할 때만 추가 쿼리
    @JoinColumn(name = "venue_id", nullable = false)  // venue_id: 명시하지 않으면 Hibernate 자동 생성; DDL과 일치시키기 위해 명시
    private Venue venue;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String artist;

    @Column(length = 50)
    private String genre;

    @Column(columnDefinition = "TEXT")  // TEXT: VARCHAR 최대 초과 가능한 긴 설명 텍스트용
    private String description;

    @Column(nullable = false)
    private LocalDateTime ticketOpenAt;  // 티켓 오픈 시각; Phase 3 오픈 스케줄러에서 이 값과 현재 시각을 비교

    public static Event create(Venue venue, String title, String artist,
                               String genre, String description, LocalDateTime ticketOpenAt) {
        Event event = new Event();
        event.venue = venue;
        event.title = title;
        event.artist = artist;
        event.genre = genre;
        event.description = description;
        event.ticketOpenAt = ticketOpenAt;
        return event;
    }
}