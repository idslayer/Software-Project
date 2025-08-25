// OccupancyItemDto.java
package com.bookingservice.dto.dashboard;

public record OccupancyItemDto(
        Long eventId,
        String title,
        int totalSeats,
        int availableSeats,
        double occupancyPct
) {}
