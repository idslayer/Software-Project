// PaymentBreakdownRow.java
package com.bookingservice.projections;

public interface PaymentBreakdownRow {
    Long getPaid();
    Long getPending();
    Long getCanceled();
}
