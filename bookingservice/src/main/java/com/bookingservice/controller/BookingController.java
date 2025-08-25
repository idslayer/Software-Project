// src/main/java/com/bookingservice/controller/BookingController.java
package com.bookingservice.controller;

import com.bookingservice.dto.*;
import com.bookingservice.entities.Booking;
import com.bookingservice.entities.Payment;
import com.bookingservice.enums.PaymentStatus;
import com.bookingservice.service.BookingService;
import com.bookingservice.service.PaymentService;
import com.bookingservice.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;



    /**
     * Tạo một booking mới (PENDING).
     *
     * @param req chứa userId, eventId, numberOfTickets
     * @return BookingResponse với chi tiết booking
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
        @Valid @RequestBody BookingRequest req) {

        BookingResponse resp = bookingService.createBooking(
            req.getUserId(),
            req.getEventId(),
            req.getNumberOfTickets()
        );

//        log.info("SMART-CONTRACT-BOOKING-CREATE: {}", resp);
        return ResponseEntity.ok(resp);
    }


    @PutMapping("/cancel/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(
        @PathVariable String id) {

        Booking booking = bookingService.cancelBooking(id);

        BookingResponse resp = BookingResponse.builder()
            .bookingId(booking.getId())
            .userId(booking.getUserId())
            .eventId(booking.getEventId())
            .numberOfTickets(booking.getNumberOfTickets())
            .totalPrice(booking.getTotalPrice())
            .status(booking.getStatus().name())
            .createdAt(booking.getCreatedAt())
            .build();
        log.info("SMART-CONTRACT-BOOKING-CANCEL: {}", resp);
        return ResponseEntity.ok(resp);
    }

    @Operation(
        summary = "Get bookings (paging, sorting, filters)",
        description = """
            Filters:
            - userId, eventId, status
            - createdFrom, createdTo (ISO-8601)
            - minTickets, maxTickets
            - minTotalPrice, maxTotalPrice
            - q (search in id or userId)
            
            Sorting: use sort=property,(asc|desc). Multiple sort params supported.
            Examples:
            ?sort=createdAt,desc
            ?sort=status,asc&sort=createdAt,desc
            """
    )
    @GetMapping
    public PageResponse<BookingResponse> findAll(
        @ParameterObject BookingFilter filter,
        @ParameterObject
        @PageableDefault(size = 20)
        @SortDefault.SortDefaults({
            @SortDefault(sort = "createdAt")
        }) Pageable pageable
    ) {
        return bookingService.search(filter, pageable);
    }
}
