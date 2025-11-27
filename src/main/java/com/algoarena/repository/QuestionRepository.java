// src/main/java/com/algoarena/repository/QuestionRepository.java
package com.algoarena.repository;

import com.algoarena.model.Question;
import com.algoarena.model.QuestionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    // Using categoryId instead of category.id
    List<Question> findByCategoryId(String categoryId);
    Page<Question> findByCategoryId(String categoryId, Pageable pageable);
    
    List<Question> findByLevel(QuestionLevel level);
    List<Question> findByCategoryIdAndLevel(String categoryId, QuestionLevel level);

    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Question> findByTitleContainingIgnoreCase(String title);

    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'statement': { $regex: ?0, $options: 'i' } } ] }")
    List<Question> searchByTitleOrStatement(String searchTerm);

    List<Question> findByCreatedById(String createdById);

    long countByCategoryId(String categoryId);
    long countByLevel(QuestionLevel level);

    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Question> findByCategoryIdOrderByCreatedAtDesc(String categoryId, Pageable pageable);

    @Query(value = "{ 'categoryId': ?0 }", fields = "{ 'title': 1, 'level': 1, 'createdAt': 1 }")
    List<Question> findQuestionSummaryByCategory(String categoryId);

    @Query(value = "{ 'title': { $regex: ?0, $options: 'i' } }", exists = true)
    boolean existsByTitleIgnoreCase(String title);

    long countByDisplayOrderIsNull();
    List<Question> findByCategoryIdAndLevelAndDisplayOrderIsNull(String categoryId, QuestionLevel level);
    List<Question> findByDisplayOrderIsNull();

    Optional<Question> findTop1ByCategoryIdAndLevelOrderByDisplayOrderDesc(String categoryId, QuestionLevel level);

    // Migration helpers
    @Query(value = "{ 'category': { $exists: true } }")
    List<Question> findQuestionsWithOldCategoryDBRef();

    @Query(value = "{ 'createdBy': { $exists: true } }")
    List<Question> findQuestionsWithOldCreatedByDBRef();
}