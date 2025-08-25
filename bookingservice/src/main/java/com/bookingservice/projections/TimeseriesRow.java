// TimeseriesRow.java
package com.bookingservice.projections;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

public interface TimeseriesRow {
    Instant getDay();    // from date_trunc('day', timestamp)
    BigDecimal getRevenue();
    Long getTickets();
    Long getOrders_paid();
}
