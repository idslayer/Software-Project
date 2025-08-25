// src/main/java/com/bookingservice/dto/BookingResponse.java
package com.bookingservice.dto;

import com.bookingservice.entities.Booking;
import com.bookingservice.entities.Event;
import com.bookingservice.repository.EventRepository;
import com.bookingservice.util.BeanUtils;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookingResponse {
    private String bookingId;
    private String userId;
    private Long eventId;
    private String eventTitle;
    private Instant eventTime;
    private String eventLocation;
    private int numberOfTickets;
    private Double totalPrice;
    private String status;
    private Instant createdAt;
    private String sessionUrl;
    private String message;

    public BookingResponse(Booking booking) {
        this.bookingId = booking.getId();
        this.userId = booking.getUserId();
        this.eventId = booking.getEventId();
        this.numberOfTickets = booking.getNumberOfTickets();
        this.totalPrice = booking.getTotalPrice();
        this.status = booking.getStatus().name();
        this.createdAt = booking.getCreatedAt();
        this.sessionUrl = booking.getSessionUrl();
        EventRepository eventRepository = BeanUtils.getBean(EventRepository.class);
        Event event = eventRepository.findById(booking.getEventId()).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        this.eventTitle = event.getTitle();
        this.eventTime = event.getStartTime();
        this.eventLocation = event.getLocation();

    }
}

