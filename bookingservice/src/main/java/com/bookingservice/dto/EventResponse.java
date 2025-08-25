package com.bookingservice.dto;

import com.bookingservice.entities.Event;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String brief;
    private String description;
    private String location;
    private Instant startTime;
    private Instant endTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double ticketPrice;
    private Instant createdAt;
    private String pictureUrl;

    public EventResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.brief = event.getBrief();
        this.description = event.getDescription();
        this.location = event.getLocation();
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
        this.totalSeats = event.getTotalSeats();
        this.availableSeats = event.getAvailableSeats();
        this.ticketPrice = event.getTicketPrice();
        this.createdAt = event.getCreatedAt();
        this.pictureUrl = event.getPictureUrl();

    }
}

