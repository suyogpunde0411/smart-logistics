package com.smartlogistics.service;

import com.smartlogistics.dto.LoadRequest;
import com.smartlogistics.exception.BadRequestException;
import com.smartlogistics.exception.ForbiddenException;
import com.smartlogistics.model.Bid;
import com.smartlogistics.model.Load;
import com.smartlogistics.model.LocationAddress;
import com.smartlogistics.model.Trip;
import com.smartlogistics.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private LocationHistoryRepository locationHistoryRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private BusinessService businessService;

    private Load sampleLoad;
    private Bid sampleBid;

    @BeforeEach
    void setUp() {
        sampleLoad = Load.builder()
                .id("load123")
                .businessId("business123")
                .source(LocationAddress.builder().address("Mumbai").lat(19.076).lng(72.877).build())
                .destination(LocationAddress.builder().address("Pune").lat(18.520).lng(73.856).build())
                .cargoType("Steel")
                .cargoWeight(10.0)
                .vehicleType("heavy")
                .budget(25000.0)
                .status("OPEN")
                .build();

        sampleBid = Bid.builder()
                .id("bid123")
                .loadId("load123")
                .driverId("driver123")
                .amount(24000.0)
                .status("PENDING")
                .build();
    }

    @Test
    void testCreateLoad() {
        LoadRequest req = LoadRequest.builder()
                .source(LocationAddress.builder().address("Mumbai").build())
                .destination(LocationAddress.builder().address("Pune").build())
                .cargoType("Steel")
                .cargoWeight(10.0)
                .vehicleType("heavy")
                .pickupDate(Instant.now())
                .deliveryDate(Instant.now().plusSeconds(86400))
                .budget(25000.0)
                .build();

        when(loadRepository.save(any(Load.class))).thenReturn(sampleLoad);

        Load created = businessService.createLoad("business123", req);

        assertNotNull(created);
        assertEquals("load123", created.getId());
        assertEquals("OPEN", created.getStatus());
    }

    @Test
    void testAcceptBidSuccess() {
        when(bidRepository.findById("bid123")).thenReturn(Optional.of(sampleBid));
        when(loadRepository.findById("load123")).thenReturn(Optional.of(sampleLoad));

        Trip sampleTrip = Trip.builder()
                .id("trip123")
                .loadId("load123")
                .bidId("bid123")
                .driverId("driver123")
                .businessId("business123")
                .status("ASSIGNED")
                .build();

        when(tripRepository.save(any(Trip.class))).thenReturn(sampleTrip);

        Trip trip = businessService.acceptBid("bid123", "business123");

        assertNotNull(trip);
        assertEquals("trip123", trip.getId());
        assertEquals("ACCEPTED", sampleBid.getStatus());
        assertEquals("ASSIGNED", sampleLoad.getStatus());
        verify(mongoTemplate, times(1)).updateMulti(any(Query.class), any(Update.class), eq(Bid.class));
    }

    @Test
    void testAcceptBidUnauthorizedThrowsForbidden() {
        when(bidRepository.findById("bid123")).thenReturn(Optional.of(sampleBid));
        when(loadRepository.findById("load123")).thenReturn(Optional.of(sampleLoad));

        assertThrows(ForbiddenException.class, () -> businessService.acceptBid("bid123", "wrongBusiness"));
    }
}
