// src/main/java/com/bookingservice/api/BookingFilter.java
package com.bookingservice.dto;

import com.bookingservice.entities.Booking;
import com.bookingservice.entities.Event;
import com.bookingservice.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.Predicate;
import lombok.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Filters for searching bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingFilter {
    @Schema(example = "user-123")
    public String userId;

    @Schema(example = "42")
    public Long eventId;

    @Schema(example = "PENDING")
    public BookingStatus status;

    @Schema(description = "Created from (ISO-8601)", example = "2025-08-01T00:00:00Z")
    public Instant createdFrom;

    @Schema(description = "Created to (ISO-8601)", example = "2025-08-31T23:59:59Z")
    public Instant createdTo;

    @Schema(example = "0")
    public Integer minTickets;

    @Schema(example = "10")
    public Integer maxTickets;

    @Schema(example = "0")
    public Double minTotalPrice;

    @Schema(example = "1000")
    public Double maxTotalPrice;

    @Schema(description = "Free text search: matches id or userId (contains)", example = "BK-2025")
    public String q;

    public Specification<Booking> getSpecification(){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
