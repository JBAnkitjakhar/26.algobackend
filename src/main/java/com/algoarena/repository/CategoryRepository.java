// File: src/main/java/com/algoarena/repository/CategoryRepository.java
package com.algoarena.repository;

import com.algoarena.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Optional<Category> findByNameIgnoreCase(String name);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' } }", exists = true)
    boolean existsByNameIgnoreCase(String name);

    List<Category> findAllByOrderByDisplayOrderAscCreatedAtAscNameAsc();
    
    List<Category> findAllByOrderByNameAsc();
    
    List<Category> findAllByOrderByCreatedAtAsc();

    @Query(value = "{}", count = true)
    long countAllCategories();

    // UPDATED: Changed from findByCreatedBy_Id to findByCreatedById (denormalized field)
    List<Category> findByCreatedById(String createdById);

    @Query("{ 'createdAt': { $gte: ?0 } }")
    List<Category> findCategoriesCreatedAfter(java.time.LocalDateTime date);
    
    @Query(value = "{ 'displayOrder': null }", count = true)
    long countByDisplayOrderIsNull();
    
    @Query("{ 'displayOrder': null }")
    List<Category> findByDisplayOrderIsNull();
    
    Optional<Category> findTopByOrderByDisplayOrderDesc();
}