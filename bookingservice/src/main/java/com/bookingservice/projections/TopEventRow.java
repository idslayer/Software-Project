// TopEventRow.java
package com.bookingservice.projections;

import java.math.BigDecimal;

public interface TopEventRow {
    Long getEventId();
    String getTitle();
    BigDecimal getRevenue();
    Long getTickets();
}
