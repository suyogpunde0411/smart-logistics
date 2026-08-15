package com.smartlogistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverProfileRequest {
    private String name;
    private String phone;
    private String licenseNumber;
    private String aadhaarNumber;
    private String address;
    private String city;
    private String state;
}
