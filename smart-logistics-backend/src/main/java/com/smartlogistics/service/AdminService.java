package com.smartlogistics.service;

import com.smartlogistics.dto.AdminStatsDto;
import com.smartlogistics.dto.TruckResponseDto;
import com.smartlogistics.dto.UserSummaryDto;
import com.smartlogistics.dto.VerifyDocRequest;
import com.smartlogistics.exception.BadRequestException;
import com.smartlogistics.exception.ResourceNotFoundException;
import com.smartlogistics.model.Truck;
import com.smartlogistics.model.User;
import com.smartlogistics.repository.LoadRepository;
import com.smartlogistics.repository.TripRepository;
import com.smartlogistics.repository.TruckRepository;
import com.smartlogistics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final LoadRepository loadRepository;
    private final TripRepository tripRepository;
    private final TruckRepository truckRepository;

    public AdminStatsDto getSystemStats() {
        long totalUsers = userRepository.count();
        long drivers = userRepository.countByRole("driver");
        long businesses = userRepository.countByRole("business");
        long activeLoads = loadRepository.countByStatus("OPEN");
        long activeTrips = tripRepository.countByStatusIn(List.of("ASSIGNED", "READY", "IN_TRANSIT"));

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .drivers(drivers)
                .businesses(businesses)
                .activeLoads(activeLoads)
                .activeTrips(activeTrips)
                .build();
    }

    public List<UserSummaryDto> getAllUsers() {
        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
        return users.stream().map(u -> UserSummaryDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .phone(u.getPhone())
                .isVerified(u.getIsVerified())
                .build()
        ).toList();
    }

    public List<TruckResponseDto> getPendingDocuments() {
        List<Truck> trucks = truckRepository.findPendingVerificationTrucks();
        return trucks.stream().map(truck -> {
            UserSummaryDto driverSummary = null;
            if (truck.getDriver() != null) {
                driverSummary = userRepository.findById(truck.getDriver())
                        .map(u -> UserSummaryDto.builder()
                                .id(u.getId())
                                .name(u.getName())
                                .email(u.getEmail())
                                .phone(u.getPhone())
                                .build())
                        .orElse(null);
            }

            return TruckResponseDto.builder()
                    .id(truck.getId())
                    .driver(driverSummary != null ? driverSummary : truck.getDriver())
                    .truckNumber(truck.getTruckNumber())
                    .truckType(truck.getTruckType())
                    .capacityTons(truck.getCapacityTons())
                    .imageUrls(truck.getImageUrls())
                    .rcDocUrl(truck.getRcDocUrl())
                    .pucDocUrl(truck.getPucDocUrl())
                    .insuranceDocUrl(truck.getInsuranceDocUrl())
                    .permitDocUrl(truck.getPermitDocUrl())
                    .isActive(truck.getIsActive())
                    .rcStatus(truck.getRcStatus())
                    .pucStatus(truck.getPucStatus())
                    .insuranceStatus(truck.getInsuranceStatus())
                    .permitStatus(truck.getPermitStatus())
                    .rcDetails(truck.getRcDetails())
                    .pucDetails(truck.getPucDetails())
                    .insuranceDetails(truck.getInsuranceDetails())
                    .permitDetails(truck.getPermitDetails())
                    .currentLocation(truck.getCurrentLocation())
                    .createdAt(truck.getCreatedAt())
                    .updatedAt(truck.getUpdatedAt())
                    .build();
        }).toList();
    }

    public Truck verifyDocument(String truckId, VerifyDocRequest req) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        String docType = req.getDocType() != null ? req.getDocType().toLowerCase() : "";
        String action = req.getAction() != null ? req.getAction().toLowerCase() : "";

        if (!List.of("puc", "insurance", "permit").contains(docType)) {
            throw new BadRequestException("Invalid document type: " + docType);
        }
        if (!List.of("approve", "reject").contains(action)) {
            throw new BadRequestException("Invalid action. Must be 'approve' or 'reject'");
        }

        String newStatus = "approve".equals(action) ? "verified" : "rejected";
        String note = "approve".equals(action) ? "Manually verified by Admin" : "Manually rejected by Admin";

        if ("puc".equals(docType)) {
            truck.setPucStatus(newStatus);
            Map<String, Object> details = truck.getPucDetails() != null ? new HashMap<>(truck.getPucDetails()) : new HashMap<>();
            details.put("note", note);
            truck.setPucDetails(details);
        } else if ("insurance".equals(docType)) {
            truck.setInsuranceStatus(newStatus);
            Map<String, Object> details = truck.getInsuranceDetails() != null ? new HashMap<>(truck.getInsuranceDetails()) : new HashMap<>();
            details.put("note", note);
            truck.setInsuranceDetails(details);
        } else if ("permit".equals(docType)) {
            truck.setPermitStatus(newStatus);
            Map<String, Object> details = truck.getPermitDetails() != null ? new HashMap<>(truck.getPermitDetails()) : new HashMap<>();
            details.put("note", note);
            truck.setPermitDetails(details);
        }

        return truckRepository.save(truck);
    }
}
