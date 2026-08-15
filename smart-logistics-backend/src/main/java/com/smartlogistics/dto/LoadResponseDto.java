package com.smartlogistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartlogistics.model.LocationAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadResponseDto {
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    private Object businessId; // Can be String ID or UserSummaryDto
    private LocationAddress source;
    private LocationAddress destination;
    private String cargoType;
    private Double cargoWeight;
    private String vehicleType;
    private Instant pickupDate;
    private Instant deliveryDate;
    private Double budget;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
