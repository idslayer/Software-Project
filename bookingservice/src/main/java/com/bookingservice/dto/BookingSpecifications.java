// src/main/java/com/bookingservice/repository/BookingSpecifications.java
package com.bookingservice.dto;

import com.bookingservice.entities.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class BookingSpecifications {

    private BookingSpecifications() {}

    public static Specification<Booking> build(BookingFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (f != null) {
                if (f.userId != null && !f.userId.isBlank()) {
                    predicates.add(cb.equal(root.get("userId"), f.userId));
                }
                if (f.eventId != null) {
                    predicates.add(cb.equal(root.get("eventId"), f.eventId));
                }
                if (f.status != null) {
                    predicates.add(cb.equal(root.get("status"), f.status));
                }
                if (f.createdFrom != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.createdFrom));
                }
                if (f.createdTo != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.createdTo));
                }
                if (f.minTickets != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("numberOfTickets"), f.minTickets));
                }
                if (f.maxTickets != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("numberOfTickets"), f.maxTickets));
                }
                if (f.minTotalPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("totalPrice"), f.minTotalPrice));
                }
                if (f.maxTotalPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("totalPrice"), f.maxTotalPrice));
                }
                if (f.q != null && !f.q.isBlank()) {
                    String like = "%" + f.q.toLowerCase() + "%";
                    predicates.add(cb.or(
                        cb.like(cb.lower(root.get("id")), like),
                        cb.like(cb.lower(root.get("userId")), like)
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
