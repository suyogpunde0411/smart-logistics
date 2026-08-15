package com.smartlogistics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "loads")
public class Load {

    @Id
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    @Indexed
    private String businessId; // Business User ID

    private LocationAddress source;
    private LocationAddress destination;

    private String cargoType;
    private Double cargoWeight;
    private String vehicleType;

    private Instant pickupDate;
    private Instant deliveryDate;

    private Double budget;
    private String description;

    @Indexed
    @Builder.Default
    private String status = "OPEN"; // "OPEN", "BIDDING", "ASSIGNED", "IN_TRANSIT", "DELIVERED", "CANCELLED", "COMPLETED"

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
