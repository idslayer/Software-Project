package com.bookingservice.controller;

import com.bookingservice.dto.ProductRequest;
import com.bookingservice.dto.StripeResponse;
import com.bookingservice.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Tạo Stripe Checkout Session
     */
    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkout(@RequestBody ProductRequest request)
        throws StripeException {
        StripeResponse response = paymentService.createCheckoutSession(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/payment/success/{id}")
    public ResponseEntity<String> success(@PathVariable Long id) {
        paymentService.handleCheckoutCompleted(id);
        return ResponseEntity.ok("Payment completed successfully. Booking updated.");
    }

    @PutMapping("/payment/fail/{id}")
    public ResponseEntity<String> fail(@PathVariable Long id) {
        paymentService.handleCheckoutFail(id);
        return ResponseEntity.ok("Payment Fail. Booking updated.");
    }
}
