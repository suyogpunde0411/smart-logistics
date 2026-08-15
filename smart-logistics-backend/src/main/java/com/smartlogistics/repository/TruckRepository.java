package com.smartlogistics.repository;

import com.smartlogistics.model.Truck;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TruckRepository extends MongoRepository<Truck, String> {
    List<Truck> findByDriver(String driver);
    Optional<Truck> findByTruckNumber(String truckNumber);

    @Query("{ '$or': [ { 'pucStatus': 'uploaded' }, { 'insuranceStatus': 'uploaded' }, { 'permitStatus': 'uploaded' } ] }")
    List<Truck> findPendingVerificationTrucks();
}
