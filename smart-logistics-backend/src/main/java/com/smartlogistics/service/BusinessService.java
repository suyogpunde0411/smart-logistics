package com.smartlogistics.service;

import com.smartlogistics.dto.*;
import com.smartlogistics.exception.BadRequestException;
import com.smartlogistics.exception.ForbiddenException;
import com.smartlogistics.exception.ResourceNotFoundException;
import com.smartlogistics.model.*;
import com.smartlogistics.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private static final Logger log = LoggerFactory.getLogger(BusinessService.class);

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final LoadRepository loadRepository;
    private final BidRepository bidRepository;
    private final TripRepository tripRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final MongoTemplate mongoTemplate;

    public BusinessProfileDto getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BusinessProfile profile = businessProfileRepository.findByUser(userId)
                .orElseGet(() -> businessProfileRepository.save(
                        BusinessProfile.builder()
                                .user(userId)
                                .companyName(user.getName() != null ? user.getName() : "My Business")
                                .build()
                ));

        return mapToBusinessProfileDto(profile, user);
    }

    public BusinessProfileDto updateProfile(String userId, UpdateBusinessProfileRequest req) {
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

        String companyName = user.getName();
        BusinessProfile profile = businessProfileRepository.findByUser(userId)
                .orElseGet(() -> BusinessProfile.builder().user(userId).companyName(companyName).build());

        if (req.getCompanyName() != null) profile.setCompanyName(req.getCompanyName().trim());
        if (req.getGstNumber() != null) profile.setGstNumber(req.getGstNumber().trim());
        if (req.getAddress() != null) profile.setAddress(req.getAddress().trim());
        if (req.getCity() != null) profile.setCity(req.getCity().trim());
        if (req.getState() != null) profile.setState(req.getState().trim());

        profile = businessProfileRepository.save(profile);
        return mapToBusinessProfileDto(profile, user);
    }

    public Load createLoad(String businessId, LoadRequest req) {
        Load load = Load.builder()
                .businessId(businessId)
                .source(req.getSource())
                .destination(req.getDestination())
                .cargoType(req.getCargoType())
                .cargoWeight(req.getCargoWeight())
                .vehicleType(req.getVehicleType())
                .pickupDate(req.getPickupDate())
                .deliveryDate(req.getDeliveryDate())
                .budget(req.getBudget())
                .description(req.getDescription())
                .status("OPEN")
                .build();

        return loadRepository.save(load);
    }

    public List<Load> listLoads(String businessId) {
        return loadRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    public Load updateLoad(String loadId, String businessId, LoadRequest req) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found"));

        if (!businessId.equals(load.getBusinessId())) {
            throw new ForbiddenException("Not authorized to modify this load");
        }

        if (req.getSource() != null) load.setSource(req.getSource());
        if (req.getDestination() != null) load.setDestination(req.getDestination());
        if (req.getCargoType() != null) load.setCargoType(req.getCargoType());
        if (req.getCargoWeight() != null) load.setCargoWeight(req.getCargoWeight());
        if (req.getVehicleType() != null) load.setVehicleType(req.getVehicleType());
        if (req.getPickupDate() != null) load.setPickupDate(req.getPickupDate());
        if (req.getDeliveryDate() != null) load.setDeliveryDate(req.getDeliveryDate());
        if (req.getBudget() != null) load.setBudget(req.getBudget());
        if (req.getDescription() != null) load.setDescription(req.getDescription());
        if (req.getStatus() != null) load.setStatus(req.getStatus());

        return loadRepository.save(load);
    }

    public Load cancelLoad(String loadId, String businessId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found"));

        if (!businessId.equals(load.getBusinessId())) {
            throw new ForbiddenException("Not authorized to cancel this load");
        }

        load.setStatus("CANCELLED");
        return loadRepository.save(load);
    }

    public List<BidResponseDto> listLoadBids(String loadId, String businessId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found"));

        if (!businessId.equals(load.getBusinessId())) {
            throw new ForbiddenException("Not authorized to view bids for this load");
        }

        List<Bid> bids = bidRepository.findByLoadIdOrderByAmountAsc(loadId);
        return bids.stream().map(bid -> {
            UserSummaryDto driverUser = null;
            if (bid.getDriverId() != null) {
                driverUser = userRepository.findById(bid.getDriverId())
                        .map(u -> UserSummaryDto.builder()
                                .id(u.getId())
                                .name(u.getName())
                                .phone(u.getPhone())
                                .email(u.getEmail())
                                .isVerified(u.getIsVerified())
                                .build())
                        .orElse(null);
            }

            return BidResponseDto.builder()
                    .id(bid.getId())
                    .loadId(bid.getLoadId())
                    .driverId(driverUser != null ? driverUser : bid.getDriverId())
                    .amount(bid.getAmount())
                    .estimatedDelivery(bid.getEstimatedDelivery())
                    .message(bid.getMessage())
                    .status(bid.getStatus())
                    .createdAt(bid.getCreatedAt())
                    .updatedAt(bid.getUpdatedAt())
                    .build();
        }).toList();
    }

    public Trip acceptBid(String bidId, String businessId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));

        Load load = loadRepository.findById(bid.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated load not found"));

        if (!businessId.equals(load.getBusinessId())) {
            throw new ForbiddenException("Not authorized to accept bids for this load");
        }

        if (!"OPEN".equalsIgnoreCase(load.getStatus())) {
            throw new BadRequestException("Load is no longer open");
        }

        // Accept this bid
        bid.setStatus("ACCEPTED");
        bidRepository.save(bid);

        // Reject other bids for this load
        Query rejectQuery = new Query(Criteria.where("loadId").is(load.getId()).and("_id").ne(bid.getId()));
        Update rejectUpdate = new Update().set("status", "REJECTED");
        mongoTemplate.updateMulti(rejectQuery, rejectUpdate, Bid.class);

        // Update load status
        load.setStatus("ASSIGNED");
        loadRepository.save(load);

        // Create Trip
        Trip trip = Trip.builder()
                .loadId(load.getId())
                .bidId(bid.getId())
                .driverId(bid.getDriverId())
                .businessId(load.getBusinessId())
                .source(load.getSource())
                .destination(load.getDestination())
                .status("ASSIGNED")
                .build();

        return tripRepository.save(trip);
    }

    public Bid rejectBid(String bidId, String businessId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));

        Load load = loadRepository.findById(bid.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated load not found"));

        if (!businessId.equals(load.getBusinessId())) {
            throw new ForbiddenException("Not authorized to reject this bid");
        }

        bid.setStatus("REJECTED");
        return bidRepository.save(bid);
    }

    public List<TripResponseDto> listMyTrips(String businessId) {
        List<Trip> trips = tripRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        return trips.stream().map(this::mapToPopulatedTripDto).toList();
    }

    public TripResponseDto getTripDetails(String tripId, String businessId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!businessId.equals(trip.getBusinessId())) {
            throw new ForbiddenException("Not authorized to view this trip");
        }

        return mapToPopulatedTripDto(trip);
    }

    public List<LocationHistory> getTripLocationHistory(String tripId, String businessId) {
        getTripDetails(tripId, businessId); // verify authorization
        return locationHistoryRepository.findByTripOrderByTimestampAsc(tripId);
    }

    private BusinessProfileDto mapToBusinessProfileDto(BusinessProfile p, User u) {
        return BusinessProfileDto.builder()
                .id(p.getId())
                .user(p.getUser())
                .companyName(p.getCompanyName())
                .name(u != null ? u.getName() : "")
                .email(u != null ? u.getEmail() : "")
                .phone(u != null ? u.getPhone() : "")
                .role(u != null ? u.getRole() : "business")
                .gstNumber(p.getGstNumber())
                .address(p.getAddress())
                .city(p.getCity())
                .state(p.getState())
                .verificationStatus(p.getVerificationStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private TripResponseDto mapToPopulatedTripDto(Trip trip) {
        Object loadObj = trip.getLoadId() != null ?
                loadRepository.findById(trip.getLoadId()).orElse(null) : null;

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
}
