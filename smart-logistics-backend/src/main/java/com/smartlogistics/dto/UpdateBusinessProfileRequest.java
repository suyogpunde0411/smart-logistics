package com.smartlogistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBusinessProfileRequest {
    private String name;
    private String phone;
    private String companyName;
    private String gstNumber;
    private String address;
    private String city;
    private String state;
}
