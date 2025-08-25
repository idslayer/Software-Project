package com.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    private String bookingId;
    private Double amount;
    private Long quantity;
    private String name;
    private String currency;
    private Long paymentId;
}
