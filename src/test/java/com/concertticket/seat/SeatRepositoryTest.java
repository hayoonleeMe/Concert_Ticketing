package com.concertticket.seat;

import com.concertticket.event.*;
import com.concertticket.venue.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SeatRepositoryTest {

    @Autowired private SeatRepository seatRepository;
    @Autowired private EventScheduleRepository eventScheduleRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private TestEntityManager em;

    private EventSchedule savedSchedule;

    @BeforeEach
    void setUp() {
        Venue venue = venueRepository.save(Venue.create("올림픽홀", "서울시 송파구", 5000));
        Event event = eventRepository.save(
                Event.create(venue, "BTS 콘서트", "BTS", "K-POP", null,
                        LocalDateTime.now().plusDays(30)));
        savedSchedule = eventScheduleRepository.save(
                EventSchedule.create(event,
                        LocalDateTime.now().plusDays(30),
                        LocalDateTime.now().plusDays(30).plusHours(2), 3));
    }

    @Test
    @DisplayName("Seat 최초 저장 시 version 필드의 초기값은 0이다")
    void version_초기값_검증() {
        Seat seat = seatRepository.save(
                Seat.create(savedSchedule, "A-1", BigDecimal.valueOf(50000)));
        em.flush();
        em.clear();

        Seat found = seatRepository.findById(seat.getId()).orElseThrow();

        assertThat(found.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("seat.reserve() 호출 후 flush/clear/재조회 시 version이 1이다")
    void version_수정_후_증가_검증() {
        Seat saved = seatRepository.save(
                Seat.create(savedSchedule, "A-2", BigDecimal.valueOf(50000)));
        em.flush();
        em.clear();

        Seat seat = seatRepository.findById(saved.getId()).orElseThrow();
        seat.reserve();
        em.flush();
        em.clear();

        Seat updated = seatRepository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("특정 회차의 AVAILABLE 좌석만 조회된다")
    void findByEventScheduleIdAndStatus_조회_검증() {
        seatRepository.save(Seat.create(savedSchedule, "A-1", BigDecimal.valueOf(50000)));
        seatRepository.save(Seat.create(savedSchedule, "A-2", BigDecimal.valueOf(50000)));
        Seat reserved = seatRepository.save(
                Seat.create(savedSchedule, "A-3", BigDecimal.valueOf(50000)));
        em.flush();
        em.clear();

        Seat toReserve = seatRepository.findById(reserved.getId()).orElseThrow();
        toReserve.reserve();
        em.flush();

        List<Seat> available = seatRepository.findByEventScheduleIdAndStatus(
                savedSchedule.getId(), SeatStatus.AVAILABLE);

        assertThat(available).hasSize(2);
    }
}