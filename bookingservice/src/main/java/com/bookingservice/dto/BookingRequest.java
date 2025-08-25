package com.bookingservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull
    private String userId;

    @NotNull
    private Long eventId;

    @Min(1)
    private int numberOfTickets;
}
