package com.smartlogistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartlogistics.model.CurrentLocationInfo;
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
public class TripResponseDto {
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    private Object loadId;
    private Object bidId;
    private Object driverId;
    private Object businessId;

    private LocationAddress source;
    private LocationAddress destination;
    private String status;
    private CurrentLocationInfo currentLocation;

    private Instant startedAt;
    private Instant deliveredAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
