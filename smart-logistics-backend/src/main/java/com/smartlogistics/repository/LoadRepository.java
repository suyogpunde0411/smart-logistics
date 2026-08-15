package com.smartlogistics.repository;

import com.smartlogistics.model.Load;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoadRepository extends MongoRepository<Load, String> {
    List<Load> findByBusinessIdOrderByCreatedAtDesc(String businessId);
    long countByStatus(String status);
}
