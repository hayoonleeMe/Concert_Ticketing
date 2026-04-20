package com.concertticket.event;

import com.concertticket.common.response.ApiResponse;
import com.concertticket.event.dto.CreateEventRequest;
import com.concertticket.event.dto.CreateScheduleRequest;
import com.concertticket.event.dto.EventResponse;
import com.concertticket.event.dto.ScheduleResponse;
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

/** 공연 및 회차 HTTP API Controller. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/admin/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> create(
            @RequestBody @Valid CreateEventRequest request) {
        EventResponse response = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    // @PathVariable: URL 경로 변수 {id}를 메서드 파라미터로 바인딩
    @PostMapping("/admin/events/{id}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> addSchedule(
            @PathVariable Long id,
            @RequestBody @Valid CreateScheduleRequest request) {
        ScheduleResponse response = eventService.addSchedule(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }
}