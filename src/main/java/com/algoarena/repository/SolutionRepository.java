// src/main/java/com/algoarena/repository/SolutionRepository.java
package com.algoarena.repository;

import com.algoarena.model.Solution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends MongoRepository<Solution, String> {

    // ✅ UPDATED: Use questionId field directly (no more question.$id!)
    List<Solution> findByQuestionId(String questionId);
    
    List<Solution> findByQuestionIdOrderByCreatedAtAsc(String questionId);

    long countByQuestionId(String questionId);

    Page<Solution> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Solution> findByQuestionIdOrderByCreatedAtDesc(String questionId, Pageable pageable);

    // Find solutions with visualizers
    @Query("{ 'visualizerFileIds': { $exists: true, $not: { $size: 0 } } }")
    List<Solution> findSolutionsWithVisualizers();

    // Find solutions with images
    @Query("{ 'imageUrls': { $exists: true, $not: { $size: 0 } } }")
    List<Solution> findSolutionsWithImages();

    @Query("{ 'youtubeLink': { $exists: true, $ne: null, $ne: '' } }")
    List<Solution> findSolutionsWithYoutubeVideos();

    @Query("{ 'driveLink': { $exists: true, $ne: null, $ne: '' } }")
    List<Solution> findSolutionsWithDriveLinks();

    @Query("{ $and: [ " +
           "{ 'youtubeLink': { $exists: true, $ne: null, $ne: '' } }, " +
           "{ 'driveLink': { $exists: true, $ne: null, $ne: '' } } " +
           "] }")
    List<Solution> findSolutionsWithBothLinks();

    void deleteByQuestionId(String questionId);

    // Simplified summary query
    @Query(value = "{ 'questionId': ?0 }", 
           fields = "{ 'content': 1, 'createdByName': 1, 'createdAt': 1, 'driveLink': 1, 'youtubeLink': 1 }")
    List<Solution> findSolutionSummaryByQuestion(String questionId);

    // Count queries
    @Query(value = "{ 'youtubeLink': { $exists: true, $ne: null, $ne: '' } }", count = true)
    long countSolutionsWithYoutubeVideos();

    @Query(value = "{ 'driveLink': { $exists: true, $ne: null, $ne: '' } }", count = true)
    long countSolutionsWithDriveLinks();

    @Query(value = "{ 'imageUrls': { $exists: true, $not: { $size: 0 } } }", count = true)
    long countSolutionsWithImages();

    @Query(value = "{ 'visualizerFileIds': { $exists: true, $not: { $size: 0 } } }", count = true)
    long countSolutionsWithVisualizers();
}