package com.smartlogistics.repository;

import com.smartlogistics.model.DocumentStore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentStoreRepository extends MongoRepository<DocumentStore, String> {
    Optional<DocumentStore> findByUserAndDocTypeAndFileHash(String user, String docType, String fileHash);
}
