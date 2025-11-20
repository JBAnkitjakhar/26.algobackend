// src/main/java/com/algoarena/service/dsa/CategoryService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.CategoryDTO;
import com.algoarena.dto.user.GlobalCategoryInfoDTO;  // ← ADD THIS IMPORT
import com.algoarena.model.Category;
import com.algoarena.model.Question;
import com.algoarena.model.QuestionLevel;
import com.algoarena.model.User;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.repository.ApproachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private ApproachRepository approachRepository;

    @Autowired
    private UserProgressService userProgressService;

    /**
     * GET /api/categories
     * Returns Map<String, CategoryDTO> with category name as key
     * Response format:
     * {
     *   "Arrays": { id, name, displayOrder, questionIds by level, counts, ... },
     *   "HashMap": { ... },
     *   ...
     * }
     */
    @Cacheable(value = "adminCategories")
    public Map<String, CategoryDTO> getAllCategories() {
        System.out.println("CACHE MISS: Fetching all categories from database");
        
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
        
        Map<String, CategoryDTO> categoryMap = new LinkedHashMap<>();
        
        for (Category category : categories) {
            CategoryDTO dto = CategoryDTO.fromEntity(category);
            categoryMap.put(category.getName(), dto);
        }
        
        return categoryMap;
    }

    /**
     * GET /api/categories/{id}
     * Returns single category by ID
     */
    public CategoryDTO getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return CategoryDTO.fromEntity(category);
    }

    /**
     * GET /api/user/categories/info
     * Get global categories info for frontend (UserController endpoint)
     * Uses data directly from category model - NO EXTRA QUERIES!
     * This is SUPER FAST - just 1 database query for all categories
     */
    @Cacheable(value = "globalCategories")
    public GlobalCategoryInfoDTO getGlobalCategoriesInfo() {
        System.out.println("CACHE MISS: Fetching global categories info from database");
        
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
        Map<String, GlobalCategoryInfoDTO.CategoryInfo> categoryInfoMap = new HashMap<>();
        
        for (Category category : categories) {
            GlobalCategoryInfoDTO.CategoryInfo info = new GlobalCategoryInfoDTO.CategoryInfo();
            info.setId(category.getId());
            info.setName(category.getName());
            
            // Use data directly from category model - NO EXTRA QUERIES!
            info.setEasyQuestionIds(new ArrayList<>(category.getEasyQuestionIds()));
            info.setMediumQuestionIds(new ArrayList<>(category.getMediumQuestionIds()));
            info.setHardQuestionIds(new ArrayList<>(category.getHardQuestionIds()));
            
            info.setEasyCount(category.getEasyCount());
            info.setMediumCount(category.getMediumCount());
            info.setHardCount(category.getHardCount());
            info.setTotalQuestions(category.getTotalQuestions());
            
            categoryInfoMap.put(category.getId(), info);
        }
        
        GlobalCategoryInfoDTO result = new GlobalCategoryInfoDTO();
        result.setCategories(categoryInfoMap);
        
        System.out.println("✓ Loaded " + categories.size() + " categories in ONE query!");
        
        return result;
    }

    /**
     * POST /api/categories
     * Create new category with auto-assigned displayOrder
     */
    @CacheEvict(value = { "adminCategories", "globalCategories", "categoriesProgress" }, allEntries = true)
    public CategoryDTO createCategory(CategoryDTO categoryDTO, User createdBy) {
        // Check if category name already exists
        if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new RuntimeException("Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        Category category = new Category();
        category.setName(categoryDTO.getName().trim());
        category.setCreatedBy(createdBy);
        
        // Auto-assign displayOrder (highest + 1)
        Integer maxOrder = categoryRepository.findTopByOrderByDisplayOrderDesc()
                .map(Category::getDisplayOrder)
                .orElse(0);
        category.setDisplayOrder(maxOrder + 1);
        
        // Initialize empty lists (already done in constructor, but explicit)
        category.setEasyQuestionIds(new ArrayList<>());
        category.setMediumQuestionIds(new ArrayList<>());
        category.setHardQuestionIds(new ArrayList<>());
        category.recalculateCounts();
        
        Category savedCategory = categoryRepository.save(category);
        
        System.out.println("✓ Created category: " + savedCategory.getName() + 
                         " (displayOrder: " + savedCategory.getDisplayOrder() + ")");
        
        return CategoryDTO.fromEntity(savedCategory);
    }

    /**
     * PUT /api/categories/{id}
     * Update category name and/or displayOrder
     */
    @CacheEvict(value = { "adminCategories", "globalCategories", "categoriesProgress" }, allEntries = true)
    public CategoryDTO updateCategory(String id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if new name conflicts with existing category (except current one)
        if (categoryDTO.getName() != null && !categoryDTO.getName().equalsIgnoreCase(category.getName())) {
            Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCase(categoryDTO.getName());
            if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
                throw new RuntimeException("Category with name '" + categoryDTO.getName() + "' already exists");
            }
            category.setName(categoryDTO.getName().trim());
        }
        
        // Update displayOrder if provided
        if (categoryDTO.getDisplayOrder() != null) {
            category.setDisplayOrder(categoryDTO.getDisplayOrder());
        }

        Category updatedCategory = categoryRepository.save(category);
        
        System.out.println("✓ Updated category: " + updatedCategory.getName());

        return CategoryDTO.fromEntity(updatedCategory);
    }

    /**
     * DELETE /api/categories/{id}
     * Delete category and all its questions (cascade)
     */
    @CacheEvict(value = { "adminCategories", "globalCategories", "categoriesProgress", 
                          "questionsMetadata", "userProgressMap", "userMeStats" }, allEntries = true)
    @Transactional
    public Map<String, Object> deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Get all questions in this category
        List<Question> questions = questionRepository.findByCategory_Id(id);
        int deletedQuestionsCount = questions.size();

        List<String> questionIds = questions.stream()
                .map(Question::getId)
                .toList();

        if (!questionIds.isEmpty()) {
            // Delete solutions for all questions
            for (String questionId : questionIds) {
                solutionRepository.deleteByQuestion_Id(questionId);
                approachRepository.deleteByQuestion_Id(questionId);
            }
            
            // Remove questions from user progress
            int removedFromUsers = userProgressService.removeQuestionsFromAllUsers(questionIds);
            System.out.println("✓ Removed " + removedFromUsers + " question entries from users' progress");

            // Delete all questions
            questionRepository.deleteAll(questions);
        }

        // Delete category
        categoryRepository.deleteById(id);

        System.out.println("✓ Deleted category '" + category.getName() + "' and " + 
                         deletedQuestionsCount + " questions");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Category deleted successfully");
        result.put("categoryName", category.getName());
        result.put("deletedQuestions", deletedQuestionsCount);
        
        return result;
    }

    /**
     * Helper method: Add question to category's question list
     * Called from QuestionService when creating/updating questions
     */
    @CacheEvict(value = { "adminCategories", "globalCategories" }, allEntries = true)
    public void addQuestionToCategory(String categoryId, String questionId, QuestionLevel level) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.addQuestionId(questionId, level);
        categoryRepository.save(category);
        
        System.out.println("✓ Added question to category '" + category.getName() + "' (" + level + ")");
    }

    /**
     * Helper method: Remove question from category's question list
     * Called from QuestionService when deleting questions
     */
    @CacheEvict(value = { "adminCategories", "globalCategories" }, allEntries = true)
    public void removeQuestionFromCategory(String categoryId, String questionId, QuestionLevel level) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.removeQuestionId(questionId, level);
        categoryRepository.save(category);
        
        System.out.println("✓ Removed question from category '" + category.getName() + "' (" + level + ")");
    }

    /**
     * Helper method: Move question between categories or levels
     * Called from QuestionService when updating question category/level
     */
    @CacheEvict(value = { "adminCategories", "globalCategories" }, allEntries = true)
    public void moveQuestion(String oldCategoryId, String newCategoryId, 
                            String questionId, QuestionLevel oldLevel, QuestionLevel newLevel) {
        // Remove from old category
        if (oldCategoryId != null && !oldCategoryId.equals(newCategoryId)) {
            Category oldCategory = categoryRepository.findById(oldCategoryId).orElse(null);
            if (oldCategory != null) {
                oldCategory.removeQuestionId(questionId, oldLevel);
                categoryRepository.save(oldCategory);
                System.out.println("✓ Removed question from old category: " + oldCategory.getName());
            }
        } else if (oldCategoryId != null && oldCategoryId.equals(newCategoryId) && oldLevel != newLevel) {
            // Same category, different level - remove from old level
            Category category = categoryRepository.findById(oldCategoryId).orElse(null);
            if (category != null) {
                category.removeQuestionId(questionId, oldLevel);
                categoryRepository.save(category);
            }
        }
        
        // Add to new category
        if (newCategoryId != null) {
            Category newCategory = categoryRepository.findById(newCategoryId)
                    .orElseThrow(() -> new RuntimeException("New category not found"));
            newCategory.addQuestionId(questionId, newLevel);
            categoryRepository.save(newCategory);
            System.out.println("✓ Added question to new category: " + newCategory.getName() + " (" + newLevel + ")");
        }
    }

    /**
     * Admin: Update category display order
     */
    @CacheEvict(value = { "adminCategories", "globalCategories" }, allEntries = true)
    public CategoryDTO updateCategoryDisplayOrder(String id, Integer newOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setDisplayOrder(newOrder);
        Category updated = categoryRepository.save(category);
        
        System.out.println("✓ Updated displayOrder for '" + category.getName() + "' to " + newOrder);
        
        return CategoryDTO.fromEntity(updated);
    }

    /**
     * Admin: Batch update category display orders
     */
    @CacheEvict(value = { "adminCategories", "globalCategories" }, allEntries = true)
    public List<CategoryDTO> batchUpdateDisplayOrder(Map<String, Integer> orderMap) {
        List<Category> updatedCategories = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : orderMap.entrySet()) {
            String categoryId = entry.getKey();
            Integer newOrder = entry.getValue();
            
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                category.setDisplayOrder(newOrder);
                updatedCategories.add(categoryRepository.save(category));
            }
        }
        
        System.out.println("✓ Batch updated displayOrder for " + updatedCategories.size() + " categories");
        
        return updatedCategories.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Utility methods
    public boolean existsById(String id) {
        return categoryRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    public long getTotalCategoriesCount() {
        return categoryRepository.countAllCategories();
    }
}
 