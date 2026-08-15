package com.smartlogistics.repository;

import com.smartlogistics.model.LocationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationHistoryRepository extends MongoRepository<LocationHistory, String> {
    List<LocationHistory> findByTripOrderByTimestampAsc(String trip);
}
