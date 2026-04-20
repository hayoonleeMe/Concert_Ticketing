package com.concertticket.venue;

import com.concertticket.common.response.ApiResponse;
import com.concertticket.venue.dto.CreateVenueRequest;
import com.concertticket.venue.dto.VenueResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 공연장 HTTP API Controller. Admin 엔드포인트는 ADMIN 역할을 강제합니다. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    // @Valid: @RequestBody 파싱 후 CreateVenueRequest 필드 검증 실행
    // 검증 실패 → MethodArgumentNotValidException → GlobalExceptionHandler → 400 응답
    @PostMapping("/admin/venues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VenueResponse>> create(
            @RequestBody @Valid CreateVenueRequest request) {
        VenueResponse response = venueService.create(request);
        // 등록 성공 시 HTTP 201 Created 반환 (200 OK보다 의미론적으로 명확)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }
}