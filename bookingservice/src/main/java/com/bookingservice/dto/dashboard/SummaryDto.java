// SummaryDto.java
package com.bookingservice.dto.dashboard;

import java.math.BigDecimal;

public record SummaryDto(
        BigDecimal grossRevenue,
        BigDecimal netRevenue,           // nếu chưa có, trả null
        long ticketsSold,
        long ordersPaid,
        BigDecimal aov,                  // gross / ordersPaid
        BigDecimal avgTicketsPerOrder,   // tickets / ordersPaid
        double conversionRatePct,        // PAID / (PENDING+PAID)
        double abandonedRatePct,         // PENDING quá ngưỡng / (PENDING+PAID)
        BigDecimal refunds               // nếu chưa lưu refunds, trả null
) {}
