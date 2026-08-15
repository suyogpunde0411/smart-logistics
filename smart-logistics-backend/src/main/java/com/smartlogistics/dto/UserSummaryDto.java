package com.smartlogistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private String id;

    @JsonProperty("_id")
    public String get_id() {
        return id;
    }

    private String name;
    private String email;
    private String role;
    private String phone;
    private Boolean isVerified;
    private String companyName;
}
