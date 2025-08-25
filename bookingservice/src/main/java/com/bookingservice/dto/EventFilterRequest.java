package com.bookingservice.dto;

import com.bookingservice.entities.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.criteria.Predicate;
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
public class EventFilterRequest {
    private String title;
    private String brief;
    private String description;
    private String location;
    private Instant startTime;
    private Instant endTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double ticketPrice;
    @JsonIgnore
    private Pageable pageable;

    public Specification<Event> getSpecification(){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}