package com.smartlogistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartlogistics.model.LocationPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckResponseDto {
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    private Object driver; // Can be String ID or UserSummaryDto
    private String truckNumber;
    private String truckType;
    private Double capacityTons;
    private List<String> imageUrls;
    private String rcDocUrl;
    private String pucDocUrl;
    private String insuranceDocUrl;
    private String permitDocUrl;
    private Boolean isActive;
    private String rcStatus;
    private String pucStatus;
    private String insuranceStatus;
    private String permitStatus;
    private Map<String, Object> rcDetails;
    private Map<String, Object> pucDetails;
    private Map<String, Object> insuranceDetails;
    private Map<String, Object> permitDetails;
    private LocationPoint currentLocation;
    private Instant createdAt;
    private Instant updatedAt;
}
