// src/main/java/com/algoarena/repository/UserApproachesRepository.java
package com.algoarena.repository;

import com.algoarena.model.UserApproaches;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserApproaches
 * Each user has ONE document containing ALL their approaches
 */
@Repository
public interface UserApproachesRepository extends MongoRepository<UserApproaches, String> {

    /**
     * Find user's approaches document by userId
     * Since _id = userId, this is the main query method
     */
    Optional<UserApproaches> findByUserId(String userId);

    /**
     * Check if user has any approaches
     */
    boolean existsByUserId(String userId);
}