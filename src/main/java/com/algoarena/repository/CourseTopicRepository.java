// src/main/java/com/algoarena/repository/CourseTopicRepository.java
package com.algoarena.repository;

import com.algoarena.model.CourseTopic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseTopicRepository extends MongoRepository<CourseTopic, String> {

    Optional<CourseTopic> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    // NEW: Public topics only
    List<CourseTopic> findByIsPublicTrueOrderByDisplayOrderAsc();
    
    // All topics (admin)
    List<CourseTopic> findAllByOrderByDisplayOrderAsc();
    List<CourseTopic> findAllByOrderByCreatedAtDesc();

    List<CourseTopic> findByCreatedById(String createdById);
    long count();
}