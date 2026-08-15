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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "documents")
public class DocumentRecord {

    @Id
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    @Indexed
    private String driverId;

    private String documentType; // "DRIVING_LICENCE", "AADHAAR", "RC", "PUC", "INSURANCE", "PERMIT"

    @Builder.Default
    private String status = "PENDING"; // "PENDING", "PROCESSING", "VERIFIED", "REJECTED", "EXPIRED"

    @Builder.Default
    private List<String> fileUrls = new ArrayList<>();

    private Map<String, Object> extractedData;
    private Double confidence;

    @Builder.Default
    private List<String> fraudFlags = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    private Instant expiryDate;
    private Instant verifiedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
