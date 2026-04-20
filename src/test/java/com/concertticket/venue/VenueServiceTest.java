package com.concertticket.venue;

import com.concertticket.venue.dto.CreateVenueRequest;
import com.concertticket.venue.dto.VenueResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

// Spring Context 없이 Mockito만으로 VenueService 단위 테스트
@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    // @InjectMocks: 테스트 대상 클래스 — @Mock 객체가 생성자 주입됨
    @InjectMocks
    private VenueService venueService;

    @Mock
    private VenueRepository venueRepository;

    @Test
    @DisplayName("공연장 등록 성공")
    void create_success() {
        // given
        CreateVenueRequest request = new CreateVenueRequest("올림픽공원", "서울 송파구", 10000);
        Venue mockVenue = Venue.create("올림픽공원", "서울 송파구", 10000);
        // BDD 스타일: given().willReturn() — 특정 인자 호출 시 반환값 지정
        given(venueRepository.save(any(Venue.class))).willReturn(mockVenue);

        // when
        VenueResponse response = venueService.create(request);

        // then — AssertJ: JUnit assertEquals보다 가독성 높은 체이닝 API
        assertThat(response.name()).isEqualTo("올림픽공원");
        assertThat(response.address()).isEqualTo("서울 송파구");
        assertThat(response.totalCapacity()).isEqualTo(10000);
    }
}