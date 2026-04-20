package com.concertticket.seat;

import com.concertticket.common.response.ApiResponse;
import com.concertticket.seat.dto.CreateSeatsRequest;
import com.concertticket.seat.dto.SeatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 좌석 HTTP API Controller. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/admin/schedules/{id}/seats/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> bulkCreate(
            @PathVariable Long id,
            @RequestBody @Valid CreateSeatsRequest request) {
        List<SeatResponse> responses = seatService.bulkCreate(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(responses));
    }
}