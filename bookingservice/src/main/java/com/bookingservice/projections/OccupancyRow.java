// OccupancyRow.java
package com.bookingservice.projections;

public interface OccupancyRow {
    Long getEventId();
    String getTitle();
    Integer getTotalSeats();
    Integer getAvailableSeats();
    Double getOccupancyPct();
}
