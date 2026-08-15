package com.smartlogistics.dto;

import com.smartlogistics.model.LocationAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class LoadRequest {

    @NotNull(message = "Source location is required")
    @Valid
    private LocationAddress source;

    @NotNull(message = "Destination location is required")
    @Valid
    private LocationAddress destination;

    @NotBlank(message = "Cargo type is required")
    private String cargoType;

    @NotNull(message = "Cargo weight is required")
    @Positive(message = "Cargo weight must be positive")
    private Double cargoWeight;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotNull(message = "Pickup date is required")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.smartlogistics.config.FlexibleInstantDeserializer.class)
    private Instant pickupDate;

    @NotNull(message = "Delivery date is required")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.smartlogistics.config.FlexibleInstantDeserializer.class)
    private Instant deliveryDate;

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be positive")
    private Double budget;

    private String description;
    private String status;
}
