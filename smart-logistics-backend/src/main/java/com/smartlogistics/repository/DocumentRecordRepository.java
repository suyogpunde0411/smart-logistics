package com.smartlogistics.repository;

import com.smartlogistics.model.DocumentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRecordRepository extends MongoRepository<DocumentRecord, String> {
    List<DocumentRecord> findByDriverId(String driverId);
}
