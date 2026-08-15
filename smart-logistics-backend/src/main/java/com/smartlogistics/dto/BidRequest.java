package com.smartlogistics.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {

    @NotNull(message = "Bid amount is required")
    @Positive(message = "Bid amount must be positive")
    private Double amount;

    @NotNull(message = "Estimated delivery date is required")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.smartlogistics.config.FlexibleInstantDeserializer.class)
    private Instant estimatedDelivery;

    private String message;
}
