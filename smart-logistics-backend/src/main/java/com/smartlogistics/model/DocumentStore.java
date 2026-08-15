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
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "documentstores")
@CompoundIndex(name = "user_doctype_hash_idx", def = "{'user': 1, 'docType': 1, 'fileHash': 1}", unique = true)
public class DocumentStore {

    @Id
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    @Indexed
    @Field("user")
    private String user; // User ID

    private String docType;

    @Indexed
    @Field("truck")
    private String truck; // Truck ID (optional)

    private String filename;
    private String contentType;
    private String dataUrl;

    @Indexed
    private String fileHash;

    private Long fileSize;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
