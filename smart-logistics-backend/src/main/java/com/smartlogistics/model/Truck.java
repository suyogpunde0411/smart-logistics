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
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trucks")
public class Truck {

    @Id
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    @Indexed
    @Field("driver")
    private String driver; // Driver User ID

    @Indexed(unique = true)
    private String truckNumber;

    private String truckType; // "mini", "medium", "heavy", "trailer"

    private Double capacityTons;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    private String rcDocUrl;
    private String pucDocUrl;
    private String insuranceDocUrl;
    private String permitDocUrl;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private String rcStatus = "pending"; // "pending", "verified", "rejected", "uploaded"

    @Builder.Default
    private String pucStatus = "pending";

    @Builder.Default
    private String insuranceStatus = "pending";

    @Builder.Default
    private String permitStatus = "pending";

    private Map<String, Object> rcDetails;
    private Map<String, Object> pucDetails;
    private Map<String, Object> insuranceDetails;
    private Map<String, Object> permitDetails;

    private LocationPoint currentLocation;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
