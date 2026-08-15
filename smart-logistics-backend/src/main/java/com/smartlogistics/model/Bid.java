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
@Document(collection = "bids")
@CompoundIndex(name = "load_driver_unique_idx", def = "{'loadId': 1, 'driverId': 1}", unique = true)
public class Bid {

    @Id
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    @Indexed
    private String loadId;

    @Indexed
    private String driverId;

    private Double amount;
    private Instant estimatedDelivery;
    private String message;

    @Builder.Default
    private String status = "PENDING"; // "PENDING", "ACCEPTED", "REJECTED", "WITHDRAWN"

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
