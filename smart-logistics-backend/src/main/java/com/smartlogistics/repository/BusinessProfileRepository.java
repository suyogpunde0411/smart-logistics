package com.smartlogistics.repository;

import com.smartlogistics.model.BusinessProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessProfileRepository extends MongoRepository<BusinessProfile, String> {
    Optional<BusinessProfile> findByUser(String user);
    boolean existsByUser(String user);
}
