package com.smartlogistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponseDto {
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    private Object loadId; // String ID or LoadResponseDto
    private Object driverId; // String ID or UserSummaryDto
    private Double amount;
    private Instant estimatedDelivery;
    private String message;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
