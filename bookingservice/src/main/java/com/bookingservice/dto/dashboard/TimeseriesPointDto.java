// TimeseriesPointDto.java
package com.bookingservice.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimeseriesPointDto(
        LocalDate day,
        BigDecimal grossRevenue,
        long ticketsSold,
        long ordersPaid
) {}
