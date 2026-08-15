package com.smartlogistics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trips")
@CompoundIndex(name = "driver_status_idx", def = "{'driverId': 1, 'status': 1}")
@CompoundIndex(name = "business_status_idx", def = "{'businessId': 1, 'status': 1}")
public class Trip {

    @Id
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    @Indexed
    private String loadId;

    @Indexed
    private String bidId;

    @Indexed
    private String driverId;

    @Indexed
    private String businessId;

    private LocationAddress source;
    private LocationAddress destination;

    @Builder.Default
    private String status = "ASSIGNED"; // "ASSIGNED", "READY", "IN_TRANSIT", "DELIVERED", "COMPLETED"

    private CurrentLocationInfo currentLocation;

    private Instant startedAt;
    private Instant deliveredAt;
    private Instant completedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
