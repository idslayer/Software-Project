// TopEventDto.java
package com.bookingservice.dto.dashboard;

import java.math.BigDecimal;

public record TopEventDto(
        Long eventId,
        String title,
        BigDecimal revenue,
        long tickets
) {}
