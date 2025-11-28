// File: src/main/java/com/algoarena/service/dsa/CategoryService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.CategoryDTO;
import com.algoarena.dto.dsa.CategoryMetadataDTO;
import com.algoarena.model.Category;
import com.algoarena.model.Question;
import com.algoarena.model.QuestionLevel;
import com.algoarena.model.User;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.SolutionRepository;
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
    private UserProgressService userProgressService;

    @Autowired
    private ApproachService approachService;

    /**
     * GET /api/categories
     * Returns Map<String, CategoryDTO> with category name as key
     */
    @Cacheable(value = "globalCategories")
    public Map<String, CategoryDTO> getAllCategories() {
        System.out.println("CACHE MISS: Fetching all categories from database");

        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAscCreatedAtAscNameAsc();

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
    @Cacheable(value = "globalCategories", key = "#id")
    public CategoryDTO getCategoryById(String id) {
        System.out.println("CACHE MISS: Fetching category by ID: " + id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return CategoryDTO.fromEntity(category);
    }

    /**
     * GET /api/categories/metadata
     * Get lightweight category metadata (id, name, createdByName, counts, createdAt, updatedAt)
     */
    @Cacheable(value = "globalCategoriesMetadata")
    public List<CategoryMetadataDTO> getCategoriesMetadata() {
        System.out.println("Fetching category metadata (id, name, createdByName, counts, timestamps)");

        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAscCreatedAtAscNameAsc();

        return categories.stream()
                .map(category -> new CategoryMetadataDTO(
                    category.getId(), 
                    category.getName(),
                    category.getCreatedByName(),
                    category.getEasyCount(),
                    category.getMediumCount(),
                    category.getHardCount(),
                    category.getTotalQuestions(),
                    category.getCreatedAt(),
                    category.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * POST /api/categories
     * Create new category
     * UPDATED: Stores creator name and ID directly (denormalized)
     */
    @CacheEvict(value = { "globalCategories", "globalCategoriesMetadata" }, allEntries = true)
    public CategoryDTO createCategory(CategoryDTO categoryDTO, User createdBy) {
        // Check if category name already exists
        if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new RuntimeException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        Category category = new Category();
        category.setName(categoryDTO.getName().trim());
        
        // UPDATED: Store denormalized creator fields
        category.setCreatedByName(createdBy.getName());
        category.setCreatedById(createdBy.getId());

        // Use provided displayOrder OR auto-assign
        if (categoryDTO.getDisplayOrder() != null) {
            category.setDisplayOrder(categoryDTO.getDisplayOrder());
            System.out.println("Using admin-provided displayOrder: " + categoryDTO.getDisplayOrder());
        } else {
            Integer maxOrder = categoryRepository.findTopByOrderByDisplayOrderDesc()
                    .map(Category::getDisplayOrder)
                    .orElse(0);
            category.setDisplayOrder(maxOrder + 1);
            System.out.println("Auto-assigned displayOrder: " + (maxOrder + 1));
        }

        // Initialize empty lists
        category.setEasyQuestionIds(new ArrayList<>());
        category.setMediumQuestionIds(new ArrayList<>());
        category.setHardQuestionIds(new ArrayList<>());
        category.recalculateCounts();

        Category savedCategory = categoryRepository.save(category);

        System.out.println("✓ Created category: " + savedCategory.getName() +
                " by " + savedCategory.getCreatedByName() +
                " (displayOrder: " + savedCategory.getDisplayOrder() + ")");

        return CategoryDTO.fromEntity(savedCategory);
    }

    /**
     * PUT /api/categories/{id}
     * Update category name and/or displayOrder
     */
    @CacheEvict(value = { "globalCategories", "globalCategoriesMetadata" }, allEntries = true)
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

        // Update displayOrder if provided (duplicates allowed!)
        if (categoryDTO.getDisplayOrder() != null) {
            category.setDisplayOrder(categoryDTO.getDisplayOrder());
        }

        Category updatedCategory = categoryRepository.save(category);

        System.out.println("✓ Updated category: " + updatedCategory.getName());

        return CategoryDTO.fromEntity(updatedCategory);
    }
// adminQuestionsSummary,\
//   adminSolutionsSummary,\
//   globalCategories,\
//   globalCategoriesMetadata,\
//   userMeStats,\
//   questionsMetadata,\
//   questionDetail,\
//   questionSolutions,\
//   solutionDetail,\
    /**
     * DELETE /api/categories/{id}
     * Delete category and all its questions (cascade)
     */
    @CacheEvict(value = { "globalCategories", "globalCategoriesMetadata",
            "questionsMetadata", "adminQuestionsSummary", "adminSolutionsSummary", "questionDetail", "questionSolutions", "solutionDetail", "userMeStats" }, allEntries = true)
    @Transactional
    public Map<String, Object> deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Get all questions in this category
        List<Question> questions = questionRepository.findByCategoryId(id);
        int deletedQuestionsCount = questions.size();

        List<String> questionIds = questions.stream()
                .map(Question::getId)
                .toList();

        if (!questionIds.isEmpty()) {
            // Delete solutions for all questions
            for (String questionId : questionIds) {
                solutionRepository.deleteByQuestionId(questionId);
                approachService.deleteAllApproachesForQuestion(questionId);
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
     * Helper: Add question to category
     */
    @CacheEvict(value = { "globalCategories", "globalCategoriesMetadata" }, allEntries = true)
    public void addQuestionToCategory(String categoryId, String questionId, QuestionLevel level) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.addQuestionId(questionId, level);
        categoryRepository.save(category);

        System.out.println("✓ Added question to category '" + category.getName() + "' (" + level + ")");
    }

    /**
     * Helper: Remove question from category
     */
    @CacheEvict(value = { "globalCategories", "globalCategoriesMetadata" }, allEntries = true)
    public void removeQuestionFromCategory(String categoryId, String questionId, QuestionLevel level) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.removeQuestionId(questionId, level);
        categoryRepository.save(category);

        System.out.println("✓ Removed question from category '" + category.getName() + "' (" + level + ")");
    }

    /**
     * Helper: Move question between categories or levels
     */
    @CacheEvict(value = { "globalCategories", "globalCategoriesMetadata" }, allEntries = true)
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
            // Same category, different level
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