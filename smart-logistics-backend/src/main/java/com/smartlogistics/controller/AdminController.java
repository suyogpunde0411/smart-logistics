package com.smartlogistics.controller;

import com.smartlogistics.dto.AdminStatsDto;
import com.smartlogistics.dto.ApiResponse;
import com.smartlogistics.dto.TruckResponseDto;
import com.smartlogistics.dto.UserSummaryDto;
import com.smartlogistics.dto.VerifyDocRequest;
import com.smartlogistics.model.Truck;
import com.smartlogistics.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'admin')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getStats() {
        AdminStatsDto stats = adminService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getUsers() {
        List<UserSummaryDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/documents/pending")
    public ResponseEntity<ApiResponse<List<TruckResponseDto>>> getPendingDocuments() {
        List<TruckResponseDto> pending = adminService.getPendingDocuments();
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @PutMapping("/documents/verify/{id}")
    public ResponseEntity<ApiResponse<Truck>> verifyDocument(
            @PathVariable("id") String truckId,
            @Valid @RequestBody VerifyDocRequest req) {
        Truck truck = adminService.verifyDocument(truckId, req);
        String actionName = req.getAction() != null ? req.getAction() : "processed";
        return ResponseEntity.ok(ApiResponse.success(truck, "Document " + actionName + "d successfully"));
    }
}
