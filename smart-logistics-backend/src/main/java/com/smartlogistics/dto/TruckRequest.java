package com.smartlogistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckRequest {

    @NotBlank(message = "Truck number is required")
    private String truckNumber;

    @NotBlank(message = "Truck type is required")
    @Pattern(regexp = "^(mini|medium|heavy|trailer)$", message = "Truck type must be 'mini', 'medium', 'heavy', or 'trailer'")
    private String truckType;

    @NotNull(message = "Capacity in tons is required")
    @Positive(message = "Capacity must be positive")
    private Double capacityTons;

    private Boolean isActive;
}
