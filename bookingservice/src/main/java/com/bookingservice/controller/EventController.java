package com.bookingservice.controller;

import com.bookingservice.dto.EventFilterRequest;
import com.bookingservice.dto.EventRequest;
import com.bookingservice.dto.EventResponse;
import com.bookingservice.dto.PageResponse;
import com.bookingservice.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/events")
public class EventController {
    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<EventResponse>> getAll(@ParameterObject Pageable pageable, @ParameterObject EventFilterRequest request) {
        request.setPageable(pageable);
        return
            ResponseEntity.ok(service.findAll(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@RequestBody @Valid EventRequest req) {
        EventResponse resp = service.create(req);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid EventRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}