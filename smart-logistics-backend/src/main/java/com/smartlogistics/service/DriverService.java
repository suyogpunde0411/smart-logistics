package com.smartlogistics.service;

import com.smartlogistics.client.DocVerifyClient;
import com.smartlogistics.dto.*;
import com.smartlogistics.exception.BadRequestException;
import com.smartlogistics.exception.ForbiddenException;
import com.smartlogistics.exception.ResourceNotFoundException;
import com.smartlogistics.model.*;
import com.smartlogistics.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverService.class);

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final TruckRepository truckRepository;
    private final LoadRepository loadRepository;
    private final BidRepository bidRepository;
    private final TripRepository tripRepository;
    private final DocumentStoreRepository documentStoreRepository;
    private final DocVerifyClient docVerifyClient;
    private final MongoTemplate mongoTemplate;

    public DriverProfileDto getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DriverProfile profile = driverProfileRepository.findByUser(userId)
                .orElseGet(() -> driverProfileRepository.save(DriverProfile.builder().user(userId).build()));

        return mapToDriverProfileDto(profile, user);
    }

    public DriverProfileDto updateProfile(String userId, UpdateDriverProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean userUpdated = false;
        if (StringUtils.hasText(req.getName())) {
            user.setName(req.getName().trim());
            userUpdated = true;
        }
        if (StringUtils.hasText(req.getPhone())) {
            user.setPhone(req.getPhone().trim());
            userUpdated = true;
        }
        if (userUpdated) {
            user = userRepository.save(user);
        }

        DriverProfile profile = driverProfileRepository.findByUser(userId)
                .orElseGet(() -> DriverProfile.builder().user(userId).build());

        if (req.getLicenseNumber() != null) profile.setLicenseNumber(req.getLicenseNumber().trim());
        if (req.getAadhaarNumber() != null) profile.setAadhaarNumber(req.getAadhaarNumber().trim());
        if (req.getAddress() != null) profile.setAddress(req.getAddress().trim());
        if (req.getCity() != null) profile.setCity(req.getCity().trim());
        if (req.getState() != null) profile.setState(req.getState().trim());

        profile = driverProfileRepository.save(profile);
        return mapToDriverProfileDto(profile, user);
    }

    public Truck addTruck(String driverId, TruckRequest req) {
        Truck truck = Truck.builder()
                .driver(driverId)
                .truckNumber(req.getTruckNumber().trim())
                .truckType(req.getTruckType())
                .capacityTons(req.getCapacityTons())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();
        return truckRepository.save(truck);
    }

    public List<Truck> listTrucks(String driverId) {
        return truckRepository.findByDriver(driverId);
    }

    public Truck updateTruck(String truckId, String driverId, TruckRequest req) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        if (!driverId.equals(truck.getDriver())) {
            throw new ForbiddenException("Not authorized to modify this truck");
        }

        if (StringUtils.hasText(req.getTruckNumber())) truck.setTruckNumber(req.getTruckNumber().trim());
        if (StringUtils.hasText(req.getTruckType())) truck.setTruckType(req.getTruckType());
        if (req.getCapacityTons() != null) truck.setCapacityTons(req.getCapacityTons());
        if (req.getIsActive() != null) truck.setIsActive(req.getIsActive());

        return truckRepository.save(truck);
    }

    public void deleteTruck(String truckId, String driverId) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        if (!driverId.equals(truck.getDriver())) {
            throw new ForbiddenException("Not authorized to delete this truck");
        }

        truckRepository.deleteById(truckId);
    }

    public DocumentStore saveDocumentToDb(String userId, String docType, MultipartFile file, String truckId) {
        if (file == null || file.isEmpty()) return null;

        try {
            byte[] bytes = file.getBytes();
            String fileHash = calculateSha256(bytes);

            Optional<DocumentStore> existing = documentStoreRepository
                    .findByUserAndDocTypeAndFileHash(userId, docType, fileHash);
            if (existing.isPresent()) {
                log.info("Duplicate document detected (Hash: {}...). Reusing existing document ID.", fileHash.substring(0, 10));
                return existing.get();
            }

            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String dataUrl = "data:" + contentType + ";base64," + base64;

            DocumentStore doc = DocumentStore.builder()
                    .user(userId)
                    .docType(docType)
                    .truck(truckId)
                    .filename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document")
                    .contentType(contentType)
                    .dataUrl(dataUrl)
                    .fileHash(fileHash)
                    .fileSize(file.getSize())
                    .build();

            return documentStoreRepository.save(doc);
        } catch (IOException e) {
            throw new BadRequestException("Failed to process file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyLicense(String userId, MultipartFile frontFile, MultipartFile backFile) {
        if (frontFile == null || frontFile.isEmpty() || backFile == null || backFile.isEmpty()) {
            throw new BadRequestException("Both front and back license images are required");
        }

        DocumentStore frontDoc = saveDocumentToDb(userId, "license_front", frontFile, null);
        saveDocumentToDb(userId, "license_back", backFile, null);

        Map<String, Object> result = docVerifyClient.verifyDrivingLicenseCombined(frontFile, backFile);

        Map<String, Object> frontMap = (Map<String, Object>) result.get("front");
        Map<String, Object> backMap = (Map<String, Object>) result.get("back");
        Map<String, Object> dataMap = (Map<String, Object>) result.get("data");

        String fraudStatus = (String) result.get("fraud_status");
        String licenseNum = null;
        if (frontMap != null && frontMap.get("licence_number") != null) {
            licenseNum = (String) frontMap.get("licence_number");
        } else if (dataMap != null && dataMap.get("license_number") != null) {
            licenseNum = (String) dataMap.get("license_number");
        }

        String verificationStatus = "clean".equalsIgnoreCase(fraudStatus) ? "verified" :
                (StringUtils.hasText(licenseNum) ? "verified" : "rejected");

        DriverProfile profile = driverProfileRepository.findByUser(userId)
                .orElseGet(() -> DriverProfile.builder().user(userId).build());

        if (licenseNum != null) profile.setLicenseNumber(licenseNum);
        if (frontDoc != null) profile.setLicenseDocUrl(frontDoc.getDataUrl());
        profile.setVerificationStatus(verificationStatus);

        Map<String, Object> licenseDetails = new HashMap<>();
        if (frontMap != null) licenseDetails.put("front", frontMap);
        if (backMap != null) licenseDetails.put("back", backMap);
        profile.setLicenseDetails(licenseDetails);

        driverProfileRepository.save(profile);
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyAadhaar(String userId, MultipartFile frontFile, MultipartFile backFile) {
        if (frontFile == null || frontFile.isEmpty() || backFile == null || backFile.isEmpty()) {
            throw new BadRequestException("Both Aadhaar front and back images are required");
        }

        DocumentStore frontDoc = saveDocumentToDb(userId, "aadhaar_front", frontFile, null);
        saveDocumentToDb(userId, "aadhaar_back", backFile, null);

        Map<String, Object> result = docVerifyClient.verifyAadhaar(frontFile, backFile);

        Map<String, Object> frontMap = (Map<String, Object>) result.get("front");
        Map<String, Object> backMap = (Map<String, Object>) result.get("back");

        String fraudStatus = (String) result.get("fraud_status");
        String aadhaarNum = null;
        if (frontMap != null && frontMap.get("aadhaar_number") != null) {
            aadhaarNum = (String) frontMap.get("aadhaar_number");
        } else if (backMap != null && backMap.get("aadhaar_number") != null) {
            aadhaarNum = (String) backMap.get("aadhaar_number");
        }

        boolean hasName = frontMap != null && frontMap.get("name") != null;
        String aadhaarStatus = ("clean".equalsIgnoreCase(fraudStatus) || StringUtils.hasText(aadhaarNum) || hasName)
                ? "verified" : "rejected";

        DriverProfile profile = driverProfileRepository.findByUser(userId)
                .orElseGet(() -> DriverProfile.builder().user(userId).build());

        if (aadhaarNum != null) profile.setAadhaarNumber(aadhaarNum);
        if (frontDoc != null) profile.setAadhaarDocUrl(frontDoc.getDataUrl());
        profile.setAadhaarStatus(aadhaarStatus);

        Map<String, Object> aadhaarDetails = new HashMap<>();
        if (frontMap != null) aadhaarDetails.put("front", frontMap);
        if (backMap != null) aadhaarDetails.put("back", backMap);
        profile.setAadhaarDetails(aadhaarDetails);

        driverProfileRepository.save(profile);
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyTruckRC(String truckId, String driverId, MultipartFile file) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        if (!driverId.equals(truck.getDriver())) {
            throw new ForbiddenException("Not authorized to verify this truck");
        }

        DocumentStore rcDoc = saveDocumentToDb(driverId, "rc", file, truckId);
        Map<String, Object> result = docVerifyClient.verifyRC(file);

        String fraudStatus = (String) result.get("fraud_status");
        String rcStatus = "clean".equalsIgnoreCase(fraudStatus) ? "verified" : "rejected";

        if (rcDoc != null) truck.setRcDocUrl(rcDoc.getDataUrl());
        truck.setRcStatus(rcStatus);
        truck.setRcDetails((Map<String, Object>) result.get("rc_fields"));

        truckRepository.save(truck);
        return result;
    }

    public Map<String, Object> verifyTruckPUC(String truckId, String driverId, MultipartFile file) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        if (!driverId.equals(truck.getDriver())) {
            throw new ForbiddenException("Not authorized to verify this truck");
        }

        DocumentStore pucDoc = saveDocumentToDb(driverId, "puc", file, truckId);
        if (pucDoc != null) truck.setPucDocUrl(pucDoc.getDataUrl());
        truck.setPucStatus("uploaded");
        truck.setPucDetails(Map.of("note", "Manual verification pending"));

        truckRepository.save(truck);
        return Map.of("success", true, "message", "PUC document uploaded successfully");
    }

    public Map<String, Object> verifyTruckInsurance(String truckId, String driverId, MultipartFile file) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        if (!driverId.equals(truck.getDriver())) {
            throw new ForbiddenException("Not authorized to verify this truck");
        }

        DocumentStore insuranceDoc = saveDocumentToDb(driverId, "insurance", file, truckId);
        if (insuranceDoc != null) truck.setInsuranceDocUrl(insuranceDoc.getDataUrl());
        truck.setInsuranceStatus("uploaded");
        truck.setInsuranceDetails(Map.of("note", "Manual verification pending"));

        truckRepository.save(truck);
        return Map.of("success", true, "message", "Insurance document uploaded successfully");
    }

    public Map<String, Object> verifyTruckPermit(String truckId, String driverId, MultipartFile file) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        if (!driverId.equals(truck.getDriver())) {
            throw new ForbiddenException("Not authorized to verify this truck");
        }

        DocumentStore permitDoc = saveDocumentToDb(driverId, "permit", file, truckId);
        if (permitDoc != null) truck.setPermitDocUrl(permitDoc.getDataUrl());
        truck.setPermitStatus("uploaded");
        truck.setPermitDetails(Map.of("note", "Manual verification pending"));

        truckRepository.save(truck);
        return Map.of("success", true, "message", "Permit document uploaded successfully");
    }

    public List<LoadResponseDto> searchLoads(Map<String, String> filters) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is("OPEN"));

        if (filters.containsKey("cargoType") && StringUtils.hasText(filters.get("cargoType"))) {
            query.addCriteria(Criteria.where("cargoType").regex(filters.get("cargoType"), "i"));
        }
        if (filters.containsKey("vehicleType") && StringUtils.hasText(filters.get("vehicleType"))) {
            query.addCriteria(Criteria.where("vehicleType").regex(filters.get("vehicleType"), "i"));
        }
        if (filters.containsKey("origin") && StringUtils.hasText(filters.get("origin"))) {
            query.addCriteria(Criteria.where("source.address").regex(filters.get("origin"), "i"));
        }
        if (filters.containsKey("destination") && StringUtils.hasText(filters.get("destination"))) {
            query.addCriteria(Criteria.where("destination.address").regex(filters.get("destination"), "i"));
        }
        if (filters.containsKey("search") && StringUtils.hasText(filters.get("search"))) {
            String searchStr = filters.get("search");
            Criteria orCriteria = new Criteria().orOperator(
                    Criteria.where("source.address").regex(searchStr, "i"),
                    Criteria.where("destination.address").regex(searchStr, "i"),
                    Criteria.where("cargoType").regex(searchStr, "i")
            );
            query.addCriteria(orCriteria);
        }

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Load> loads = mongoTemplate.find(query, Load.class);

        return loads.stream().map(this::mapToPopulatedLoadDto).toList();
    }

    public Bid placeBid(String driverId, String loadId, BidRequest req) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found"));

        if (!"OPEN".equalsIgnoreCase(load.getStatus())) {
            throw new BadRequestException("Load is not open for bidding");
        }

        if (bidRepository.existsByLoadIdAndDriverId(loadId, driverId)) {
            throw new BadRequestException("You have already placed a bid on this load.");
        }

        Bid bid = Bid.builder()
                .loadId(loadId)
                .driverId(driverId)
                .amount(req.getAmount())
                .estimatedDelivery(req.getEstimatedDelivery())
                .message(req.getMessage())
                .status("PENDING")
                .build();

        return bidRepository.save(bid);
    }

    public List<BidResponseDto> listMyBids(String driverId) {
        List<Bid> bids = bidRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        return bids.stream().map(bid -> {
            Object loadObj = loadRepository.findById(bid.getLoadId())
                    .map(this::mapToPopulatedLoadDto)
                    .orElse(null);

            return BidResponseDto.builder()
                    .id(bid.getId())
                    .loadId(loadObj != null ? loadObj : bid.getLoadId())
                    .driverId(bid.getDriverId())
                    .amount(bid.getAmount())
                    .estimatedDelivery(bid.getEstimatedDelivery())
                    .message(bid.getMessage())
                    .status(bid.getStatus())
                    .createdAt(bid.getCreatedAt())
                    .updatedAt(bid.getUpdatedAt())
                    .build();
        }).toList();
    }

    public List<TripResponseDto> listMyTrips(String driverId) {
        List<Trip> trips = tripRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        return trips.stream().map(this::mapToPopulatedTripDto).toList();
    }

    public TripResponseDto getTripDetails(String tripId, String driverId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!driverId.equals(trip.getDriverId())) {
            throw new ForbiddenException("Not authorized to view this trip");
        }

        return mapToPopulatedTripDto(trip);
    }

    // Helper mappings
    private DriverProfileDto mapToDriverProfileDto(DriverProfile p, User u) {
        return DriverProfileDto.builder()
                .id(p.getId())
                .user(p.getUser())
                .name(u != null ? u.getName() : "")
                .email(u != null ? u.getEmail() : "")
                .phone(u != null ? u.getPhone() : "")
                .role(u != null ? u.getRole() : "driver")
                .licenseNumber(p.getLicenseNumber())
                .licenseDocUrl(p.getLicenseDocUrl())
                .licenseDetails(p.getLicenseDetails())
                .aadhaarDocUrl(p.getAadhaarDocUrl())
                .aadhaarNumber(p.getAadhaarNumber())
                .aadhaarStatus(p.getAadhaarStatus())
                .aadhaarDetails(p.getAadhaarDetails())
                .address(p.getAddress())
                .city(p.getCity())
                .state(p.getState())
                .verificationStatus(p.getVerificationStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private LoadResponseDto mapToPopulatedLoadDto(Load load) {
        UserSummaryDto businessUser = null;
        if (load.getBusinessId() != null) {
            Optional<User> uOpt = userRepository.findById(load.getBusinessId());
            if (uOpt.isPresent()) {
                User u = uOpt.get();
                String companyName = businessProfileRepository.findByUser(u.getId())
                        .map(BusinessProfile::getCompanyName)
                        .orElse(u.getName());

                businessUser = UserSummaryDto.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .companyName(companyName)
                        .build();
            }
        }

        return LoadResponseDto.builder()
                .id(load.getId())
                .businessId(businessUser != null ? businessUser : load.getBusinessId())
                .source(load.getSource())
                .destination(load.getDestination())
                .cargoType(load.getCargoType())
                .cargoWeight(load.getCargoWeight())
                .vehicleType(load.getVehicleType())
                .pickupDate(load.getPickupDate())
                .deliveryDate(load.getDeliveryDate())
                .budget(load.getBudget())
                .description(load.getDescription())
                .status(load.getStatus())
                .createdAt(load.getCreatedAt())
                .updatedAt(load.getUpdatedAt())
                .build();
    }

    private TripResponseDto mapToPopulatedTripDto(Trip trip) {
        Object loadObj = trip.getLoadId() != null ?
                loadRepository.findById(trip.getLoadId()).map(this::mapToPopulatedLoadDto).orElse(null) : null;

        UserSummaryDto driverUser = null;
        if (trip.getDriverId() != null) {
            driverUser = userRepository.findById(trip.getDriverId())
                    .map(u -> UserSummaryDto.builder().id(u.getId()).name(u.getName()).phone(u.getPhone()).email(u.getEmail()).build())
                    .orElse(null);
        }

        UserSummaryDto businessUser = null;
        if (trip.getBusinessId() != null) {
            businessUser = userRepository.findById(trip.getBusinessId())
                    .map(u -> {
                        String companyName = businessProfileRepository.findByUser(u.getId())
                                .map(BusinessProfile::getCompanyName)
                                .orElse(u.getName());
                        return UserSummaryDto.builder().id(u.getId()).name(u.getName()).phone(u.getPhone()).email(u.getEmail()).companyName(companyName).build();
                    })
                    .orElse(null);
        }

        return TripResponseDto.builder()
                .id(trip.getId())
                .loadId(loadObj != null ? loadObj : trip.getLoadId())
                .bidId(trip.getBidId())
                .driverId(driverUser != null ? driverUser : trip.getDriverId())
                .businessId(businessUser != null ? businessUser : trip.getBusinessId())
                .source(trip.getSource())
                .destination(trip.getDestination())
                .status(trip.getStatus())
                .currentLocation(trip.getCurrentLocation())
                .startedAt(trip.getStartedAt())
                .deliveredAt(trip.getDeliveredAt())
                .completedAt(trip.getCompletedAt())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

    private String calculateSha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
