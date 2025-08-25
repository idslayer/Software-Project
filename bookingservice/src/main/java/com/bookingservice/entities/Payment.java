package com.bookingservice.entities;

import com.bookingservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String bookingId;

    private String stripePaymentId;
    private Double amount;
    private String currency = "GDP";

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.INITIATED;

    private Instant paidAt;
}

