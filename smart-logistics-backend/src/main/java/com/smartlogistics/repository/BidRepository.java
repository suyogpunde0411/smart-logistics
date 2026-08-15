package com.smartlogistics.repository;

import com.smartlogistics.model.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends MongoRepository<Bid, String> {
    List<Bid> findByDriverIdOrderByCreatedAtDesc(String driverId);
    List<Bid> findByLoadIdOrderByAmountAsc(String loadId);
    Optional<Bid> findByLoadIdAndDriverId(String loadId, String driverId);
    boolean existsByLoadIdAndDriverId(String loadId, String driverId);
}
