package com.smartlogistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfileDto {
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    private String user;
    private String name;
    private String email;
    private String phone;
    private String role;

    private String licenseNumber;
    private String licenseDocUrl;
    private Map<String, Object> licenseDetails;

    private String aadhaarDocUrl;
    private String aadhaarNumber;
    private String aadhaarStatus;
    private Map<String, Object> aadhaarDetails;

    private String address;
    private String city;
    private String state;
    private String verificationStatus;

    private Instant createdAt;
    private Instant updatedAt;
}
