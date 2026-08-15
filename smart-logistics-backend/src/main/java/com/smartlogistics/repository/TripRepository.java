package com.smartlogistics.repository;

import com.smartlogistics.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {
    List<Trip> findByDriverIdOrderByCreatedAtDesc(String driverId);
    List<Trip> findByBusinessIdOrderByCreatedAtDesc(String businessId);

    @Query(value = "{ 'status': { '$in': ?0 } }", count = true)
    long countByStatusIn(Collection<String> statuses);
}
