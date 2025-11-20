// src/main/java/com/algoarena/repository/UserProgressRepository.java
package com.algoarena.repository;

import com.algoarena.model.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProgressRepository extends MongoRepository<UserProgress, String> {
    
    Optional<UserProgress> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
    
    void deleteByUserId(String userId);
}