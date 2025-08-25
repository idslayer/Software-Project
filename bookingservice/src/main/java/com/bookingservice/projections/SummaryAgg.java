// SummaryAgg.java
package com.bookingservice.projections;

import java.math.BigDecimal;

public interface SummaryAgg {
    BigDecimal getGrossRevenue();
    Long getTicketsSold();
    Long getOrdersPaid();
}
