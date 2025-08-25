package com.bookingservice.service;

import com.bookingservice.dto.EventFilterRequest;
import com.bookingservice.dto.EventRequest;
import com.bookingservice.dto.EventResponse;

import com.bookingservice.dto.PageResponse;
import com.bookingservice.entities.Event;
import com.bookingservice.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public PageResponse<EventResponse> findAll(EventFilterRequest request) {

        return new PageResponse<>(repository.findAll(request.getSpecification(), request.getPageable())
            .map(EventResponse::new)
        );
    }

    public EventResponse findById(Long id) {
        Event e = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        return toResponse(e);
    }

    @Transactional
    public EventResponse create(EventRequest req) {
        Event e = Event.builder()
            .title(req.getTitle())
            .brief(req.getBrief())
            .description(req.getDescription())
            .location(req.getLocation())
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .totalSeats(req.getTotalSeats())
            .availableSeats(req.getTotalSeats())
            .ticketPrice(req.getTicketPrice())
            .pictureUrl(req.getPictureUrl())
            .createdAt(Instant.now())
            .build();
        Event saved = repository.save(e);
        return toResponse(saved);
    }

    @Transactional
    public EventResponse update(Long id, EventRequest req) {
        Event e = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        e.setTitle(req.getTitle());
        e.setBrief(req.getBrief());
        e.setDescription(req.getDescription());
        e.setLocation(req.getLocation());
        e.setStartTime(req.getStartTime());
        e.setEndTime(req.getEndTime());
        e.setTotalSeats(req.getTotalSeats());
        e.setAvailableSeats(req.getTotalSeats());
        e.setTicketPrice(req.getTicketPrice());
        e.setPictureUrl(req.getPictureUrl());
        Event updated = repository.save(e);
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private EventResponse toResponse(Event e) {
        return EventResponse.builder()
            .id(e.getId())
            .title(e.getTitle())
            .brief(e.getBrief())
            .description(e.getDescription())
            .location(e.getLocation())
            .startTime(e.getStartTime())
            .endTime(e.getEndTime())
            .totalSeats(e.getTotalSeats())
            .availableSeats(e.getAvailableSeats())
            .ticketPrice(e.getTicketPrice())
            .createdAt(e.getCreatedAt())
            .pictureUrl(e.getPictureUrl())
            .build();
    }
}
