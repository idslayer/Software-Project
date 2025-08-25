// PaymentBreakdownDto.java
package com.bookingservice.dto.dashboard;

public record PaymentBreakdownDto(
        long paid,
        long pending,
        long canceled
) {}
