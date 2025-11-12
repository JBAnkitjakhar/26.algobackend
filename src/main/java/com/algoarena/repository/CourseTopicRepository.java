// src/main/java/com/algoarena/repository/CourseTopicRepository.java
package com.algoarena.repository;

import com.algoarena.model.CourseTopic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseTopicRepository extends MongoRepository<CourseTopic, String> {

    // Spring Data MongoDB will automatically implement case-insensitive search
    // Just use the naming convention - no @Query needed!
    Optional<CourseTopic> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);

    // Find all topics ordered by display order
    List<CourseTopic> findAllByOrderByDisplayOrderAsc();

    // Find all topics ordered by creation date
    List<CourseTopic> findAllByOrderByCreatedAtDesc();

    // Find topics by creator
    List<CourseTopic> findByCreatedBy_Id(String createdById);

    // Count total topics
    long count();
}