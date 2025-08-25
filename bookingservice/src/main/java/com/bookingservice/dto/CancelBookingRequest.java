package com.bookingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelBookingRequest {

    @NotNull
    private String bookingId;
}
