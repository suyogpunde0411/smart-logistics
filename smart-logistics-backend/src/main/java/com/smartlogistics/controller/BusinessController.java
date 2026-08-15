package com.smartlogistics.controller;

import com.smartlogistics.dto.*;
import com.smartlogistics.model.Bid;
import com.smartlogistics.model.Load;
import com.smartlogistics.model.LocationHistory;
import com.smartlogistics.model.Trip;
import com.smartlogistics.security.CustomUserDetails;
import com.smartlogistics.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business")
@PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'business')")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<BusinessProfileDto>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BusinessProfileDto profile = businessService.getProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<BusinessProfileDto>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateBusinessProfileRequest req) {
        BusinessProfileDto profile = businessService.updateProfile(userDetails.getId(), req);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping("/loads")
    public ResponseEntity<ApiResponse<Load>> createLoad(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LoadRequest req) {
        Load load = businessService.createLoad(userDetails.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(load));
    }

    @GetMapping("/loads")
    public ResponseEntity<ApiResponse<List<Load>>> listLoads(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Load> loads = businessService.listLoads(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(loads));
    }

    @PutMapping("/loads/{id}")
    public ResponseEntity<ApiResponse<Load>> updateLoad(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody LoadRequest req) {
        Load load = businessService.updateLoad(id, userDetails.getId(), req);
        return ResponseEntity.ok(ApiResponse.success(load));
    }

    @PutMapping("/loads/{id}/cancel")
    public ResponseEntity<ApiResponse<Load>> cancelLoad(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Load load = businessService.cancelLoad(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(load));
    }

    @GetMapping("/loads/{loadId}/bids")
    public ResponseEntity<ApiResponse<List<BidResponseDto>>> listLoadBids(
            @PathVariable("loadId") String loadId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<BidResponseDto> bids = businessService.listLoadBids(loadId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    @PutMapping("/bids/{bidId}/accept")
    public ResponseEntity<ApiResponse<Trip>> acceptBid(
            @PathVariable("bidId") String bidId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Trip trip = businessService.acceptBid(bidId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(trip, "Bid accepted and Trip assigned."));
    }

    @PutMapping("/bids/{bidId}/reject")
    public ResponseEntity<ApiResponse<Bid>> rejectBid(
            @PathVariable("bidId") String bidId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Bid bid = businessService.rejectBid(bidId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(bid, "Bid rejected."));
    }

    @GetMapping("/trips")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> listMyTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TripResponseDto> trips = businessService.listMyTrips(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @GetMapping("/trips/{id}")
    public ResponseEntity<ApiResponse<TripResponseDto>> getTripDetails(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TripResponseDto trip = businessService.getTripDetails(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(trip));
    }

    @GetMapping("/trips/{id}/history")
    public ResponseEntity<ApiResponse<List<LocationHistory>>> getTripLocationHistory(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LocationHistory> history = businessService.getTripLocationHistory(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
