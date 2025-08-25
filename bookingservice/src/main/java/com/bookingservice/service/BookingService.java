package com.bookingservice.service;

import com.bookingservice.dto.*;
import com.bookingservice.entities.Booking;
import com.bookingservice.entities.Event;
import com.bookingservice.entities.Payment;
import com.bookingservice.enums.BookingStatus;
import com.bookingservice.enums.PaymentStatus;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.EventRepository;
import com.bookingservice.repository.PaymentRepository;
import com.bookingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepo;
    private final EventRepository eventRepo;

    private final UserRepository userRepo;
    private final StripeService stripeService;
    private final PaymentRepository paymentRepository;

    public static String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof OAuth2User u)) return null;
        Object v = u.getAttributes().get("appUserId");
        return String.valueOf(v);
    }
    @Transactional
    public BookingResponse createBooking(String userId, Long eventId, int qty) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getAvailableSeats() < qty) {
            throw new IllegalStateException("Not enough seats");
        }
        event.setAvailableSeats(event.getAvailableSeats() - qty);
        eventRepo.save(event);


        if (!userRepo.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        Booking booking = Booking.builder()
            .userId(userId)
            .eventId(eventId)
            .numberOfTickets(qty)
            .totalPrice(event.getTicketPrice() * qty)
            .status(BookingStatus.PENDING) // Set initial status to PENDING
            .createdAt(java.time.Instant.now())
            .build();

        booking = bookingRepo.save(booking);
        Payment payment = Payment.builder()
            .amount(event.getTicketPrice() * qty)
            .currency("GBP")
            .paymentStatus(PaymentStatus.INITIATED)
            .bookingId(booking.getId())
            .build();


        payment = paymentRepository.save(payment);
        log.info("payment id: {}", payment);
        ProductRequest productRequest = ProductRequest.builder()
            .name("Booking for " + event.getTitle())
            .bookingId(booking.getId())
            .quantity((long) qty)
            .amount(event.getTicketPrice() * qty)
            .currency("GBP")
            .paymentId(payment.getId())
            .build();
        StripeResponse stripeResponse = stripeService.checkoutProducts(productRequest);
        payment.setStripePaymentId(stripeResponse.getSessionId());
        paymentRepository.save(payment);
        log.info("SMART-CONTRACT-PAYMENT-INITIATED: {}", stripeResponse);

        booking.setSessionUrl(stripeResponse.getSessionUrl());
        bookingRepo.save(booking);

        BookingResponse resp = BookingResponse.builder()
            .bookingId(booking.getId())
            .userId(booking.getUserId())
            .eventId(booking.getEventId())
            .numberOfTickets(booking.getNumberOfTickets())
            .totalPrice(booking.getTotalPrice())
            .status(booking.getStatus().name())
            .createdAt(booking.getCreatedAt())
            .sessionUrl(booking.getSessionUrl())
            .message(stripeResponse.getMessage())
            .build();
        log.info("SMART-CONTRACT-BOOKING-CREATE: {}", resp);
        return resp;
    }

    @Transactional
    public Booking cancelBooking(String bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        Event event = eventRepo.findById(booking.getEventId())
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        event.setAvailableSeats(event.getAvailableSeats() + booking.getNumberOfTickets());
        eventRepo.save(event);

        booking.setStatus(BookingStatus.CANCELED);


        return bookingRepo.save(booking);
    }

    public PageResponse<BookingResponse> search(BookingFilter filter, Pageable pageable) {
        return new PageResponse<>(bookingRepo.findAll(BookingSpecifications.build(filter), pageable).map(BookingResponse::new));
    }

}
