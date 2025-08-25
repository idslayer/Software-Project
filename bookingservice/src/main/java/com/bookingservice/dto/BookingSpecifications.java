// src/main/java/com/bookingservice/repository/BookingSpecifications.java
package com.bookingservice.dto;

import com.bookingservice.entities.Booking;
import org.springframework.data.jpa.domain.Specification;

public final class BookingSpecifications {

    private BookingSpecifications() {}

    public static Specification<Booking> build(BookingFilter f) {
        Specification<Booking> spec = Specification.where(null);

        if (f == null) return spec;

        if (f.userId != null && !f.userId.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("userId"), f.userId));
        }
        if (f.eventId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("eventId"), f.eventId));
        }
        if (f.status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), f.status));
        }
        if (f.createdFrom != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), f.createdFrom));
        }
        if (f.createdTo != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), f.createdTo));
        }
        if (f.minTickets != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("numberOfTickets"), f.minTickets));
        }
        if (f.maxTickets != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("numberOfTickets"), f.maxTickets));
        }
        if (f.minTotalPrice != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("totalPrice"), f.minTotalPrice));
        }
        if (f.maxTotalPrice != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("totalPrice"), f.maxTotalPrice));
        }
        if (f.q != null && !f.q.isBlank()) {
            String like = "%" + f.q.toLowerCase() + "%";
            spec = spec.and((root, qy, cb) ->
                cb.or(
                    cb.like(cb.lower(root.get("id")), like),
                    cb.like(cb.lower(root.get("userId")), like)
                )
            );
        }
        return spec;
    }
}
