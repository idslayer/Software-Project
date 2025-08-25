package com.bookingservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String brief;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String location;
    private Instant startTime;
    private Instant endTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double ticketPrice;
    private Instant createdAt = Instant.now();
    @Column(columnDefinition = "TEXT")
    private String pictureUrl;
}
