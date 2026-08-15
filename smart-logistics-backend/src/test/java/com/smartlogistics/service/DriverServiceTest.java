package com.smartlogistics.service;

import com.smartlogistics.dto.BidRequest;
import com.smartlogistics.dto.TruckRequest;
import com.smartlogistics.exception.BadRequestException;
import com.smartlogistics.exception.ResourceNotFoundException;
import com.smartlogistics.model.Bid;
import com.smartlogistics.model.Load;
import com.smartlogistics.model.Truck;
import com.smartlogistics.model.User;
import com.smartlogistics.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DocumentStoreRepository documentStoreRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private DriverService driverService;

    private User sampleDriver;
    private Truck sampleTruck;
    private Load sampleLoad;

    @BeforeEach
    void setUp() {
        sampleDriver = User.builder()
                .id("driver123")
                .name("Alex Driver")
                .email("alex@driver.com")
                .role("driver")
                .phone("1234567890")
                .build();

        sampleTruck = Truck.builder()
                .id("truck123")
                .driver("driver123")
                .truckNumber("MH12AB1234")
                .truckType("medium")
                .capacityTons(5.0)
                .isActive(true)
                .build();

        sampleLoad = Load.builder()
                .id("load123")
                .status("OPEN")
                .build();
    }

    @Test
    void testAddTruck() {
        TruckRequest req = TruckRequest.builder()
                .truckNumber("MH12AB1234")
                .truckType("medium")
                .capacityTons(5.0)
                .build();

        when(truckRepository.save(any(Truck.class))).thenReturn(sampleTruck);

        Truck truck = driverService.addTruck("driver123", req);

        assertNotNull(truck);
        assertEquals("MH12AB1234", truck.getTruckNumber());
        assertEquals("driver123", truck.getDriver());
    }

    @Test
    void testPlaceBidSuccess() {
        BidRequest req = BidRequest.builder()
                .amount(15000.0)
                .estimatedDelivery(Instant.now().plusSeconds(86400))
                .message("Ready to load")
                .build();

        Bid sampleBid = Bid.builder()
                .id("bid123")
                .loadId("load123")
                .driverId("driver123")
                .amount(15000.0)
                .status("PENDING")
                .build();

        when(loadRepository.findById("load123")).thenReturn(Optional.of(sampleLoad));
        when(bidRepository.existsByLoadIdAndDriverId("load123", "driver123")).thenReturn(false);
        when(bidRepository.save(any(Bid.class))).thenReturn(sampleBid);

        Bid bid = driverService.placeBid("driver123", "load123", req);

        assertNotNull(bid);
        assertEquals("bid123", bid.getId());
        assertEquals(15000.0, bid.getAmount());
    }

    @Test
    void testPlaceBidDuplicateThrowsBadRequest() {
        BidRequest req = BidRequest.builder()
                .amount(15000.0)
                .estimatedDelivery(Instant.now().plusSeconds(86400))
                .build();

        when(loadRepository.findById("load123")).thenReturn(Optional.of(sampleLoad));
        when(bidRepository.existsByLoadIdAndDriverId("load123", "driver123")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> driverService.placeBid("driver123", "load123", req));
    }
}
