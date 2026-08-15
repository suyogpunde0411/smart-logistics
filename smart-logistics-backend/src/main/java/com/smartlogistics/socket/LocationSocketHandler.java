package com.smartlogistics.socket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.smartlogistics.model.*;
import com.smartlogistics.repository.LoadRepository;
import com.smartlogistics.repository.LocationHistoryRepository;
import com.smartlogistics.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LocationSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LocationSocketHandler.class);

    private final TripRepository tripRepository;
    private final LoadRepository loadRepository;
    private final LocationHistoryRepository locationHistoryRepository;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String userId = client.get("userId");
        String role = client.get("role");
        log.info("Socket connected: userId={}, role={}, sessionId={}", userId, role, client.getSessionId());
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String userId = client.get("userId");
        String role = client.get("role");
        log.info("Socket disconnected: userId={}, role={}, sessionId={}", userId, role, client.getSessionId());
    }

    @OnEvent("driver:start_trip")
    public void onDriverStartTrip(SocketIOClient client, Map<String, Object> data, AckRequest ackRequest) {
        String userId = client.get("userId");
        String role = client.get("role");
        String tripId = (String) data.get("tripId");

        try {
            if (!"driver".equalsIgnoreCase(role)) {
                client.sendEvent("error:tracking", Map.of("message", "Only drivers can start a trip"));
                return;
            }

            if (tripId == null) {
                client.sendEvent("error:tracking", Map.of("message", "Trip ID is required"));
                return;
            }

            Optional<Trip> tripOpt = tripRepository.findById(tripId);
            if (tripOpt.isEmpty()) {
                client.sendEvent("error:tracking", Map.of("message", "Trip not found"));
                return;
            }

            Trip trip = tripOpt.get();
            if (!userId.equals(trip.getDriverId())) {
                log.warn("Unauthorized start_trip attempt: driver={} trip={}", userId, tripId);
                client.sendEvent("error:tracking", Map.of("message", "You are not assigned to this trip"));
                return;
            }

            String room = "trip:" + tripId;
            client.joinRoom(room);
            client.set("currentTripRoom", room);
            client.set("currentTripId", tripId);

            if (!"IN_TRANSIT".equalsIgnoreCase(trip.getStatus())) {
                trip.setStatus("IN_TRANSIT");
                trip.setStartedAt(Instant.now());
                tripRepository.save(trip);

                if (trip.getLoadId() != null) {
                    loadRepository.findById(trip.getLoadId()).ifPresent(load -> {
                        load.setStatus("IN_TRANSIT");
                        loadRepository.save(load);
                    });
                }
            }

            log.info("Driver {} started trip {}, joined room {}", userId, tripId, room);
            client.sendEvent("trip:started", Map.of("tripId", tripId, "room", room));

        } catch (Exception e) {
            log.error("start_trip error: {}", e.getMessage(), e);
            client.sendEvent("error:tracking", Map.of("message", "Failed to start trip"));
        }
    }

    @OnEvent("driver:location_update")
    public void onDriverLocationUpdate(SocketIOClient client, Map<String, Object> data, AckRequest ackRequest) {
        String userId = client.get("userId");
        String role = client.get("role");
        String tripId = (String) data.get("tripId");

        try {
            if (!"driver".equalsIgnoreCase(role)) return;

            String currentTripRoom = client.get("currentTripRoom");
            String currentTripId = client.get("currentTripId");

            if (currentTripRoom == null || !tripId.equals(currentTripId)) {
                client.sendEvent("error:tracking", Map.of("message", "Not an active trip room for this trip"));
                return;
            }

            Optional<Trip> tripOpt = tripRepository.findById(tripId);
            if (tripOpt.isEmpty() || !"IN_TRANSIT".equalsIgnoreCase(tripOpt.get().getStatus())) {
                client.sendEvent("error:tracking", Map.of("message", "Trip not active for tracking"));
                return;
            }

            Trip trip = tripOpt.get();

            Double lat = data.get("lat") != null ? ((Number) data.get("lat")).doubleValue() : null;
            Double lng = data.get("lng") != null ? ((Number) data.get("lng")).doubleValue() : null;
            Double speed = data.get("speed") != null ? ((Number) data.get("speed")).doubleValue() : null;
            Double heading = data.get("heading") != null ? ((Number) data.get("heading")).doubleValue() : null;
            Double accuracy = data.get("accuracy") != null ? ((Number) data.get("accuracy")).doubleValue() : null;

            if (lat == null || lng == null) return;

            Instant now = Instant.now();

            // Update Current Location
            trip.setCurrentLocation(CurrentLocationInfo.builder()
                    .lat(lat)
                    .lng(lng)
                    .timestamp(now)
                    .build());
            tripRepository.save(trip);

            // Save to LocationHistory
            LocationHistory history = LocationHistory.builder()
                    .trip(tripId)
                    .driver(userId)
                    .lat(lat)
                    .lng(lng)
                    .speed(speed)
                    .heading(heading)
                    .accuracy(accuracy)
                    .timestamp(now)
                    .build();
            locationHistoryRepository.save(history);

            // Broadcast to room
            Map<String, Object> updatePayload = Map.of(
                    "tripId", tripId,
                    "lat", lat,
                    "lng", lng,
                    "speed", speed != null ? speed : 0.0,
                    "heading", heading != null ? heading : 0.0,
                    "accuracy", accuracy != null ? accuracy : 0.0,
                    "timestamp", now.toString()
            );

            client.getNamespace().getRoomOperations(currentTripRoom).sendEvent("location:update", updatePayload);
            log.debug("Location updated: trip={} driver={}", tripId, userId);

        } catch (Exception e) {
            log.error("location_update error: {}", e.getMessage(), e);
            client.sendEvent("error:tracking", Map.of("message", "Failed to update location"));
        }
    }

    @OnEvent("business:join_trip_room")
    public void onBusinessJoinTripRoom(SocketIOClient client, Map<String, Object> data, AckRequest ackRequest) {
        String userId = client.get("userId");
        String role = client.get("role");
        String tripId = (String) data.get("tripId");

        try {
            if (!"business".equalsIgnoreCase(role)) {
                client.sendEvent("error:tracking", Map.of("message", "Only business owners can view tracking"));
                return;
            }

            if (tripId == null) {
                client.sendEvent("error:tracking", Map.of("message", "Trip ID is required"));
                return;
            }

            Optional<Trip> tripOpt = tripRepository.findById(tripId);
            if (tripOpt.isEmpty()) {
                client.sendEvent("error:tracking", Map.of("message", "Trip not found"));
                return;
            }

            Trip trip = tripOpt.get();
            if (!userId.equals(trip.getBusinessId())) {
                log.warn("Unauthorized room join attempt: business={} trip={}", userId, tripId);
                client.sendEvent("error:tracking", Map.of("message", "You do not own this trip"));
                return;
            }

            String room = "trip:" + tripId;
            client.joinRoom(room);
            client.set("currentTripRoom", room);

            log.info("Business {} subscribed to room {}", userId, room);

            Map<String, Object> joinResponse;
            if (trip.getCurrentLocation() != null) {
                joinResponse = Map.of(
                        "tripId", tripId,
                        "currentLocation", Map.of(
                                "lat", trip.getCurrentLocation().getLat(),
                                "lng", trip.getCurrentLocation().getLng(),
                                "timestamp", trip.getCurrentLocation().getTimestamp() != null ?
                                        trip.getCurrentLocation().getTimestamp().toString() :
                                        Instant.now().toString()
                        )
                );
            } else {
                joinResponse = Map.of("tripId", tripId);
            }

            client.sendEvent("room:joined", joinResponse);

        } catch (Exception e) {
            log.error("join_trip_room error: {}", e.getMessage(), e);
            client.sendEvent("error:tracking", Map.of("message", "Failed to join tracking room"));
        }
    }

    @OnEvent("business:confirm_delivery")
    public void onBusinessConfirmDelivery(SocketIOClient client, Map<String, Object> data, AckRequest ackRequest) {
        String userId = client.get("userId");
        String role = client.get("role");
        String tripId = (String) data.get("tripId");

        try {
            if (!"business".equalsIgnoreCase(role)) {
                client.sendEvent("error:tracking", Map.of("message", "Only business owners can confirm delivery"));
                return;
            }

            if (tripId == null) return;

            Optional<Trip> tripOpt = tripRepository.findById(tripId);
            if (tripOpt.isEmpty()) {
                client.sendEvent("error:tracking", Map.of("message", "Trip not found"));
                return;
            }

            Trip trip = tripOpt.get();
            if (!userId.equals(trip.getBusinessId())) {
                log.warn("Unauthorized confirm_delivery attempt: business={} trip={}", userId, tripId);
                client.sendEvent("error:tracking", Map.of("message", "You do not own this trip"));
                return;
            }

            trip.setStatus("DELIVERED");
            trip.setDeliveredAt(Instant.now());
            tripRepository.save(trip);

            if (trip.getLoadId() != null) {
                loadRepository.findById(trip.getLoadId()).ifPresent(load -> {
                    load.setStatus("DELIVERED");
                    loadRepository.save(load);
                });
            }

            String room = "trip:" + tripId;
            client.getNamespace().getRoomOperations(room).sendEvent("trip:ended", Map.of(
                    "tripId", tripId,
                    "message", "Delivery confirmed"
            ));

            client.leaveRoom(room);
            log.info("Delivery confirmed: trip={} by business={}", tripId, userId);

        } catch (Exception e) {
            log.error("confirm_delivery error: {}", e.getMessage(), e);
            client.sendEvent("error:tracking", Map.of("message", "Failed to confirm delivery"));
        }
    }
}
