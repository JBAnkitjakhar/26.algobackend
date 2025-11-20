// src/main/java/com/algoarena/repository/CategoryRepository.java
package com.algoarena.repository;

import com.algoarena.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    // Find category by name (case-insensitive)
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Optional<Category> findByNameIgnoreCase(String name);

    // FIXED: Changed from boolean to Boolean (wrapper class)
    // OR use @Query with exists=true
    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' } }", exists = true)
    boolean existsByNameIgnoreCase(String name);

    // Find all categories sorted by displayOrder, then name
    List<Category> findAllByOrderByDisplayOrderAscNameAsc();
    
    // Find all categories sorted by name
    List<Category> findAllByOrderByNameAsc();
    
    // Find all categories sorted by creation date
    List<Category> findAllByOrderByCreatedAtAsc();

    // Count total categories
    @Query(value = "{}", count = true)
    long countAllCategories();

    // Find categories by creator
    List<Category> findByCreatedBy_Id(String createdById);

    // Find categories created after a certain date
    @Query("{ 'createdAt': { $gte: ?0 } }")
    List<Category> findCategoriesCreatedAfter(java.time.LocalDateTime date);
    
    // NEW: Count categories without displayOrder (for migration)
    @Query(value = "{ 'displayOrder': null }", count = true)
    long countByDisplayOrderIsNull();
    
    // NEW: Find categories without displayOrder
    @Query("{ 'displayOrder': null }")
    List<Category> findByDisplayOrderIsNull();
    
    // NEW: Find highest displayOrder
    Optional<Category> findTopByOrderByDisplayOrderDesc();
}