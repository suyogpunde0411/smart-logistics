package com.smartlogistics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "locationhistories")
@CompoundIndex(name = "trip_timestamp_idx", def = "{'trip': 1, 'timestamp': 1}")
public class LocationHistory {

    @Id
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    @Indexed
    @Field("trip")
    private String trip; // Trip ID

    @Indexed
    @Field("driver")
    private String driver; // Driver User ID

    private Double lat;
    private Double lng;
    private Double speed;
    private Double heading;
    private Double accuracy;

    @Indexed
    @Builder.Default
    private Instant timestamp = Instant.now();
}
