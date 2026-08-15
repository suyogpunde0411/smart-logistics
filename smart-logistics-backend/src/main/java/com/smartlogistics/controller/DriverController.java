package com.smartlogistics.controller;

import com.smartlogistics.dto.*;
import com.smartlogistics.model.Bid;
import com.smartlogistics.model.Truck;
import com.smartlogistics.security.CustomUserDetails;
import com.smartlogistics.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/driver")
@PreAuthorize("hasAnyAuthority('ROLE_DRIVER', 'driver')")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DriverProfileDto>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DriverProfileDto profile = driverService.getProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<DriverProfileDto>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateDriverProfileRequest req) {
        DriverProfileDto profile = driverService.updateProfile(userDetails.getId(), req);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping("/trucks")
    public ResponseEntity<ApiResponse<Truck>> addTruck(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TruckRequest req) {
        Truck truck = driverService.addTruck(userDetails.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(truck));
    }

    @GetMapping("/trucks")
    public ResponseEntity<ApiResponse<List<Truck>>> listTrucks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Truck> trucks = driverService.listTrucks(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(trucks));
    }

    @PutMapping("/trucks/{id}")
    public ResponseEntity<ApiResponse<Truck>> updateTruck(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TruckRequest req) {
        Truck truck = driverService.updateTruck(id, userDetails.getId(), req);
        return ResponseEntity.ok(ApiResponse.success(truck));
    }

    @DeleteMapping("/trucks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTruck(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        driverService.deleteTruck(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.successMessage("Truck deleted"));
    }

    @PostMapping(value = "/verify-license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyLicense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("front") MultipartFile front,
            @RequestParam("back") MultipartFile back) {
        Map<String, Object> result = driverService.verifyLicense(userDetails.getId(), front, back);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping(value = "/verify-aadhaar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyAadhaar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("front") MultipartFile front,
            @RequestParam("back") MultipartFile back) {
        Map<String, Object> result = driverService.verifyAadhaar(userDetails.getId(), front, back);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping(value = "/trucks/{id}/verify-rc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyTruckRC(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = driverService.verifyTruckRC(id, userDetails.getId(), file);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping(value = "/trucks/{id}/verify-puc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyTruckPUC(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = driverService.verifyTruckPUC(id, userDetails.getId(), file);
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("message")));
    }

    @PostMapping(value = "/trucks/{id}/verify-insurance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyTruckInsurance(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = driverService.verifyTruckInsurance(id, userDetails.getId(), file);
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("message")));
    }

    @PostMapping(value = "/trucks/{id}/verify-permit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyTruckPermit(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = driverService.verifyTruckPermit(id, userDetails.getId(), file);
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("message")));
    }

    @GetMapping("/loads/open")
    public ResponseEntity<ApiResponse<List<LoadResponseDto>>> searchLoads(@RequestParam Map<String, String> filters) {
        List<LoadResponseDto> loads = driverService.searchLoads(filters);
        return ResponseEntity.ok(ApiResponse.success(loads));
    }

    @PostMapping("/loads/{loadId}/bids")
    public ResponseEntity<ApiResponse<Bid>> placeBid(
            @PathVariable("loadId") String loadId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BidRequest req) {
        Bid bid = driverService.placeBid(userDetails.getId(), loadId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(bid));
    }

    @GetMapping("/bids")
    public ResponseEntity<ApiResponse<List<BidResponseDto>>> listMyBids(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<BidResponseDto> bids = driverService.listMyBids(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    @GetMapping("/trips")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> listMyTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TripResponseDto> trips = driverService.listMyTrips(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @GetMapping("/trips/{id}")
    public ResponseEntity<ApiResponse<TripResponseDto>> getTripDetails(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TripResponseDto trip = driverService.getTripDetails(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(trip));
    }
}
