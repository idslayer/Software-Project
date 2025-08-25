package com.bookingservice.dto;

import com.bookingservice.entities.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {
    @NotBlank
    private String title;
    private String brief;
    @NotBlank
    private String description;
    @NotBlank
    private String location;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;
    @Min(1)
    private Integer totalSeats;
    @Min(1)
    private Integer availableSeats;
    @DecimalMin("0.0")
    private Double ticketPrice;
    private String pictureUrl;

}