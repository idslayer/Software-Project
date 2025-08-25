package com.bookingservice.entities;

import com.bookingservice.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    private String userId;

    private Long eventId;

    private Integer numberOfTickets;
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private Instant createdAt = Instant.now();
    @Column(columnDefinition = "TEXT")
    private String sessionUrl;
}

