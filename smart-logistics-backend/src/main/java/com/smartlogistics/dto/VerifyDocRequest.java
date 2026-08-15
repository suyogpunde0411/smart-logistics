package com.smartlogistics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDocRequest {

    @NotBlank(message = "docType is required")
    private String docType; // "puc", "insurance", "permit"

    @NotBlank(message = "action is required")
    private String action; // "approve", "reject"
}
