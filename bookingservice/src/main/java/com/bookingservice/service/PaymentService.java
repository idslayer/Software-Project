package com.bookingservice.service;

import com.bookingservice.dto.ProductRequest;
import com.bookingservice.dto.StripeResponse;
import com.bookingservice.entities.Booking;
import com.bookingservice.entities.Payment;
import com.bookingservice.enums.BookingStatus;
import com.bookingservice.enums.PaymentStatus;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final StripeService stripeService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;


    /**
     * Tạo Stripe Checkout Session dựa trên ProductRequest (có chứa bookingId)
     * và lưu lại record Payment với sessionId để theo dõi.
     */
//    @Transactional
    public StripeResponse createCheckoutSession(ProductRequest request) throws StripeException {

        // Lưu thông tin payment với sessionId
        Payment payment = Payment.builder()
            .bookingId(request.getBookingId())
            .amount(request.getAmount())
            .currency(request.getCurrency() != null ? request.getCurrency() : "GBP")
            .paymentStatus(PaymentStatus.INITIATED)
            .build();


        payment = paymentRepository.save(payment);
        log.info("payment id: {}", payment);
        request.setPaymentId(payment.getId());
        StripeResponse stripeResponse = stripeService.checkoutProducts(request);
        payment.setStripePaymentId(stripeResponse.getSessionId());
        savePayment(payment);
        log.info("SMART-CONTRACT-PAYMENT-INITIATED: {}", stripeResponse);
        return stripeResponse;
    }

    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

    /**
     * Gọi lại khi webhook về thành công (checkout.session.completed)
     * để cập nhật trạng thái payment & booking.
     */
    @Transactional
    public void handleCheckoutCompleted(Long id) {
        // Tìm payment record theo session id
        paymentRepository.findById(id)
            .ifPresent(payment -> {
                // Cập nhật lại trạng thái
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(
                    Instant.now());
                paymentRepository.save(payment);
                log.info("SMART-CONTRACT-PAYMENT-SUCCESS: {}", payment);
                // Cập nhật booking thành CONFIRMED nếu có liên kết
                Booking booking = bookingRepository.findById(payment.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid booking id"));
                if (booking != null) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);
                }
            });
    }

    @Transactional
    public void handleCheckoutFail(Long id) {
        // Tìm payment record theo session id
        paymentRepository.findById(id)
            .ifPresent(payment -> {
                // Cập nhật lại trạng thái
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.info("SMART-CONTRACT-PAYMENT-FAIL: {}", payment);
            });
    }
}
