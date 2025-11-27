// src/main/java/com/algoarena/repository/CourseDocRepository.java
package com.algoarena.repository;

import com.algoarena.model.CourseDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseDocRepository extends MongoRepository<CourseDoc, String> {

    List<CourseDoc> findByTopicIdOrderByDisplayOrderAsc(String topicId);
    List<CourseDoc> findByTopicIdOrderByCreatedAtDesc(String topicId);

    Optional<CourseDoc> findByTitleIgnoreCaseAndTopicId(String title, String topicId);
    boolean existsByTitleIgnoreCaseAndTopicId(String title, String topicId);

    long countByTopicId(String topicId);
    List<CourseDoc> findByCreatedById(String createdById);
    long count();
}