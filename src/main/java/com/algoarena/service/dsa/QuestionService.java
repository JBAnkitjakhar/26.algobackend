// src/main/java/com/algoarena/service/dsa/QuestionService.java
package com.algoarena.service.dsa;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

import com.algoarena.dto.dsa.AdminQuestionSummaryDTO;
import com.algoarena.dto.dsa.QuestionDTO;
import com.algoarena.dto.user.QuestionsMetadataDTO;
import com.algoarena.model.QuestionLevel;
import com.algoarena.model.Question;
import com.algoarena.model.User;
import com.algoarena.model.Category;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.repository.ApproachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private ApproachRepository approachRepository;

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private CategoryService categoryService; // NEW: Inject CategoryService

    @Cacheable(value = "questionsList", key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_cat_' + (#categoryId ?: 'all') + '_lvl_' + (#level ?: 'all') + '_search_' + (#search ?: 'none')")
    public Page<Question> getAllQuestionsFiltered(Pageable pageable, String categoryId, String level, String search) {
        Page<Question> questions;

        if (search != null && !search.trim().isEmpty()) {
            List<Question> searchResults = questionRepository.searchByTitleOrStatement(search.trim());
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), searchResults.size());
            List<Question> pageContent = searchResults.subList(start, end);
            questions = new PageImpl<>(pageContent, pageable, searchResults.size());
        } else if (categoryId != null && !categoryId.isEmpty()) {
            if (level != null && !level.isEmpty()) {
                QuestionLevel questionLevel = QuestionLevel.fromString(level);
                List<Question> filteredQuestions = questionRepository.findByCategory_IdAndLevel(categoryId,
                        questionLevel);
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), filteredQuestions.size());
                List<Question> pageContent = filteredQuestions.subList(start, end);
                questions = new PageImpl<>(pageContent, pageable, filteredQuestions.size());
            } else {
                questions = questionRepository.findByCategory_Id(categoryId, pageable);
            }
        } else if (level != null && !level.isEmpty()) {
            QuestionLevel questionLevel = QuestionLevel.fromString(level);
            List<Question> filteredQuestions = questionRepository.findByLevel(questionLevel);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredQuestions.size());
            List<Question> pageContent = filteredQuestions.subList(start, end);
            questions = new PageImpl<>(pageContent, pageable, filteredQuestions.size());
        } else {
            questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return questions;
    }

    /**
     * CREATE QUESTION - UPDATED TO MAINTAIN CATEGORY LISTS
     */
    @CacheEvict(value = { "globalCategories", "adminCategories", "adminQuestionsSummary",
            "questionsMetadata" }, allEntries = true)
    @Transactional
    public QuestionDTO createQuestion(QuestionDTO questionDTO, User currentUser) {
        if (questionRepository.existsByTitleIgnoreCase(questionDTO.getTitle())) {
            throw new RuntimeException("Question with this title already exists");
        }

        Question question = new Question();
        question.setTitle(questionDTO.getTitle());
        question.setStatement(questionDTO.getStatement());
        question.setImageUrls(questionDTO.getImageUrls());
        question.setImageFolderUrl(questionDTO.getImageFolderUrl());

        if (questionDTO.getCodeSnippets() != null) {
            List<Question.CodeSnippet> snippets = questionDTO.getCodeSnippets().stream()
                    .map(dto -> {
                        Question.CodeSnippet snippet = new Question.CodeSnippet();
                        snippet.setLanguage(dto.getLanguage());
                        snippet.setCode(dto.getCode());
                        snippet.setDescription(dto.getDescription());
                        return snippet;
                    })
                    .toList();
            question.setCodeSnippets(snippets);
        }

        Category category = categoryRepository.findById(questionDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        question.setLevel(questionDTO.getLevel());

        if (questionDTO.getDisplayOrder() != null) {
            question.setDisplayOrder(questionDTO.getDisplayOrder());
        } else {
            Integer maxOrder = questionRepository.findTop1ByCategory_IdAndLevelOrderByDisplayOrderDesc(
                    category.getId(),
                    questionDTO.getLevel())
                    .map(Question::getDisplayOrder)
                    .orElse(0);
            question.setDisplayOrder(maxOrder + 1);
        }

        question.setCreatedBy(currentUser);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        // STEP 1: Save question to get ID
        Question savedQuestion = questionRepository.save(question);

        // STEP 2: Add question ID to category's question list
        categoryService.addQuestionToCategory(
                savedQuestion.getCategory().getId(),
                savedQuestion.getId(),
                savedQuestion.getLevel());

        System.out.println("✓ Created new question: " + savedQuestion.getTitle() +
                " with displayOrder: " + savedQuestion.getDisplayOrder());

        return QuestionDTO.fromEntity(savedQuestion);
    }

    /**
     * UPDATE QUESTION - UPDATED TO MAINTAIN CATEGORY LISTS
     */
    @CacheEvict(value = { "globalCategories", "adminCategories", "adminQuestionsSummary", "questionsMetadata",
            "adminQuestionDetail" }, allEntries = true)
    @Transactional
    public QuestionDTO updateQuestion(String id, QuestionDTO questionDTO) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));

        if (!question.getTitle().equals(questionDTO.getTitle()) &&
                questionRepository.existsByTitleIgnoreCase(questionDTO.getTitle())) {
            throw new RuntimeException("Question with this title already exists");
        }

        // Store old values for category list maintenance
        String oldCategoryId = question.getCategory().getId();
        QuestionLevel oldLevel = question.getLevel();

        question.setTitle(questionDTO.getTitle());
        question.setStatement(questionDTO.getStatement());
        question.setImageUrls(questionDTO.getImageUrls());
        question.setImageFolderUrl(questionDTO.getImageFolderUrl());

        if (questionDTO.getCodeSnippets() != null) {
            List<Question.CodeSnippet> snippets = questionDTO.getCodeSnippets().stream()
                    .map(dto -> {
                        Question.CodeSnippet snippet = new Question.CodeSnippet();
                        snippet.setLanguage(dto.getLanguage());
                        snippet.setCode(dto.getCode());
                        snippet.setDescription(dto.getDescription());
                        return snippet;
                    })
                    .toList();
            question.setCodeSnippets(snippets);
        }

        // Check if category or level changed
        boolean categoryChanged = !oldCategoryId.equals(questionDTO.getCategoryId());
        boolean levelChanged = oldLevel != questionDTO.getLevel();

        if (categoryChanged || levelChanged) {
            // Update category reference if changed
            if (categoryChanged) {
                Category category = categoryRepository.findById(questionDTO.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                question.setCategory(category);
            }

            // Update level if changed
            if (levelChanged) {
                question.setLevel(questionDTO.getLevel());
            }

            // CRITICAL: Update category question lists
            categoryService.moveQuestion(
                    oldCategoryId,
                    question.getCategory().getId(),
                    question.getId(),
                    oldLevel,
                    question.getLevel());
        }

        if (questionDTO.getDisplayOrder() != null) {
            question.setDisplayOrder(questionDTO.getDisplayOrder());
        }

        question.setUpdatedAt(LocalDateTime.now());

        Question updatedQuestion = questionRepository.save(question);

        System.out.println("✓ Updated question: " + updatedQuestion.getTitle() +
                " with displayOrder: " + updatedQuestion.getDisplayOrder());

        return QuestionDTO.fromEntity(updatedQuestion);
    }

    /**
     * DELETE QUESTION - UPDATED TO MAINTAIN CATEGORY LISTS
     */
    @CacheEvict(value = { "globalCategories", "adminCategories", "adminQuestionsSummary", "questionsMetadata",
            "adminQuestionDetail" }, allEntries = true)
    @Transactional
    public void deleteQuestion(String id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));

        // STEP 1: Remove question ID from category's list
        categoryService.removeQuestionFromCategory(
                question.getCategory().getId(),
                question.getId(),
                question.getLevel());

        // STEP 2: Delete related data
        solutionRepository.deleteByQuestion_Id(id);
        approachRepository.deleteByQuestion_Id(id);

        int removedFromUsers = userProgressService.removeQuestionFromAllUsers(id);
        System.out.println("✓ Removed question from " + removedFromUsers + " users' progress");

        // STEP 3: Delete question
        questionRepository.deleteById(id);
        System.out.println("✓ Deleted question: " + question.getTitle());
    }

    public Page<QuestionDTO> getAllQuestions(Pageable pageable, String categoryId, String level, String search) {
        Page<Question> questions = getAllQuestionsFiltered(pageable, categoryId, level, search);
        return questions.map(QuestionDTO::fromEntity);
    }

    public Page<QuestionDTO> getQuestionsByCategory(String categoryId, Pageable pageable) {
        Page<Question> questions = questionRepository.findByCategory_IdOrderByCreatedAtDesc(categoryId, pageable);
        return questions.map(QuestionDTO::fromEntity);
    }

    public boolean existsById(String id) {
        return questionRepository.existsById(id);
    }

    public boolean existsByTitle(String title) {
        return questionRepository.existsByTitleIgnoreCase(title);
    }

    public boolean existsByTitleAndNotId(String title, String excludeId) {
        var questions = questionRepository.findByTitleContainingIgnoreCase(title);
        return questions.stream().anyMatch(q -> q.getTitle().equalsIgnoreCase(title) && !q.getId().equals(excludeId));
    }

    @Cacheable(value = "adminStats", key = "'questionCounts'")
    public Map<String, Object> getQuestionCounts() {
        Map<String, Object> counts = new HashMap<>();

        long totalQuestions = questionRepository.count();
        counts.put("total", totalQuestions);

        Map<String, Long> levelCounts = new HashMap<>();
        levelCounts.put("easy", questionRepository.countByLevel(QuestionLevel.EASY));
        levelCounts.put("medium", questionRepository.countByLevel(QuestionLevel.MEDIUM));
        levelCounts.put("hard", questionRepository.countByLevel(QuestionLevel.HARD));
        counts.put("byLevel", levelCounts);

        List<Category> categories = categoryRepository.findAll();
        Map<String, Object> categoryStats = new HashMap<>();
        for (Category category : categories) {
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("name", category.getName());
            categoryData.put("count", questionRepository.countByCategory_Id(category.getId()));
            categoryStats.put(category.getId(), categoryData);
        }
        counts.put("byCategory", categoryStats);

        return counts;
    }

    public List<QuestionDTO> searchQuestions(String searchTerm) {
        List<Question> questions = questionRepository.searchByTitleOrStatement(searchTerm);
        return questions.stream()
                .map(QuestionDTO::fromEntity)
                .toList();
    }

    public List<QuestionDTO> getQuestionsByCreator(String creatorId) {
        List<Question> questions = questionRepository.findByCreatedBy_Id(creatorId);
        return questions.stream()
                .map(QuestionDTO::fromEntity)
                .toList();
    }

    @CacheEvict(value = { "globalCategories", "adminCategories", "adminQuestionsSummary" }, allEntries = true)
    @Transactional
    public void updateQuestionDisplayOrder(String questionId, Integer displayOrder) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        question.setDisplayOrder(displayOrder);
        questionRepository.save(question);

        System.out.println("✓ Updated displayOrder to " + displayOrder +
                " for question: " + question.getTitle());
    }

    @CacheEvict(value = { "globalCategories", "adminCategories", "adminQuestionsSummary" }, allEntries = true)
    @Transactional
    public int batchUpdateDisplayOrder(List<Map<String, Object>> updates) {
        int updatedCount = 0;

        for (Map<String, Object> update : updates) {
            String questionId = (String) update.get("questionId");
            Integer displayOrder = ((Number) update.get("displayOrder")).intValue();

            Question question = questionRepository.findById(questionId).orElse(null);
            if (question != null) {
                question.setDisplayOrder(displayOrder);
                questionRepository.save(question);
                updatedCount++;
            }
        }

        System.out.println("✓ Batch updated displayOrder for " + updatedCount + " questions");
        return updatedCount;
    }

    public List<Map<String, Object>> getQuestionsByCategoryAndLevelForOrdering(String categoryId, String levelStr) {
        QuestionLevel level = QuestionLevel.valueOf(levelStr.toUpperCase());
        List<Question> questions = questionRepository.findByCategory_IdAndLevel(categoryId, level);

        questions.sort(Comparator.comparing(
                q -> q.getDisplayOrder() != null ? q.getDisplayOrder() : Integer.MAX_VALUE));

        return questions.stream()
                .map(q -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", q.getId());
                    map.put("title", q.getTitle());
                    map.put("displayOrder", q.getDisplayOrder());
                    map.put("level", q.getLevel().toString());
                    return map;
                })
                .toList();
    }

    @CacheEvict(value = { "globalCategories", "adminCategories", "adminQuestionsSummary" }, allEntries = true)
    @Transactional
    public int resetDisplayOrder(String categoryId, String levelStr) {
        QuestionLevel level = QuestionLevel.valueOf(levelStr.toUpperCase());
        List<Question> questions = questionRepository.findByCategory_IdAndLevel(categoryId, level);

        questions.sort(Comparator
                .comparing((Question q) -> q.getDisplayOrder() != null ? q.getDisplayOrder() : Integer.MAX_VALUE)
                .thenComparing(Question::getCreatedAt));

        int order = 1;
        for (Question question : questions) {
            question.setDisplayOrder(order++);
            questionRepository.save(question);
        }

        System.out.println("✓ Reset displayOrder for " + questions.size() +
                " questions in category " + categoryId + " - " + level);

        return questions.size();
    }

    @Cacheable(value = "adminQuestionsSummary", key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<AdminQuestionSummaryDTO> getAdminQuestionsSummary(Pageable pageable) {
        System.out.println("CACHE MISS: Fetching admin questions summary from database");

        Page<Question> questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable);

        return questions.map(question -> {
            AdminQuestionSummaryDTO dto = new AdminQuestionSummaryDTO();
            dto.setId(question.getId());
            dto.setTitle(question.getTitle());
            dto.setLevel(question.getLevel());
            dto.setCategoryName(question.getCategory() != null ? question.getCategory().getName() : "Unknown");
            dto.setDisplayOrder(question.getDisplayOrder());
            dto.setImageCount(question.getImageUrls() != null ? question.getImageUrls().size() : 0);
            dto.setHasCodeSnippets(question.getCodeSnippets() != null && !question.getCodeSnippets().isEmpty());
            dto.setCreatedByName(question.getCreatedBy() != null ? question.getCreatedBy().getName() : "Unknown");
            dto.setUpdatedAt(question.getUpdatedAt());
            dto.setSolutionCount((int) solutionRepository.countByQuestion_Id(question.getId()));

            return dto;
        });
    }

    @Cacheable(value = "adminQuestionDetail", key = "#questionId")
    public QuestionDTO getAdminQuestionById(String questionId) {
        System.out.println("=== QuestionService.getAdminQuestionById ===");
        System.out.println("Looking for question with ID: " + questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    System.out.println("Question NOT FOUND in database!");
                    return new RuntimeException("Question not found with id: " + questionId);
                });

        System.out.println("Question found in database: " + question.getTitle());

        QuestionDTO questionDTO = QuestionDTO.fromEntity(question);
        questionDTO.setDisplayOrder(question.getDisplayOrder());

        System.out.println("QuestionDTO created successfully");

        return questionDTO;
    }

    @Cacheable(value = "questionsMetadata")
    public QuestionsMetadataDTO getQuestionsMetadata() {
        System.out.println("CACHE MISS: Fetching questions metadata from database");

        List<Question> allQuestions = questionRepository.findAll();

        Map<String, QuestionsMetadataDTO.QuestionMetadata> metadataMap = new HashMap<>();

        for (Question question : allQuestions) {
            String categoryName = question.getCategory() != null ? question.getCategory().getName() : "Unknown";

            QuestionsMetadataDTO.QuestionMetadata metadata = new QuestionsMetadataDTO.QuestionMetadata(
                    question.getId(),
                    question.getTitle(),
                    question.getLevel(),
                    categoryName);
            metadataMap.put(question.getId(), metadata);
        }

        QuestionsMetadataDTO result = new QuestionsMetadataDTO();
        result.setQuestions(metadataMap);

        return result;
    }
}