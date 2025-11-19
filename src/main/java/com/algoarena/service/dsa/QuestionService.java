// src/main/java/com/algoarena/service/dsa/QuestionService.java  

package com.algoarena.service.dsa;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

import com.algoarena.dto.dsa.AdminQuestionSummaryDTO;
import com.algoarena.dto.dsa.QuestionDTO;
import com.algoarena.dto.dsa.QuestionDetailDTO;
import com.algoarena.dto.dsa.QuestionSummaryDTO;
import com.algoarena.dto.dsa.SolutionDTO;
import com.algoarena.dto.user.QuestionsMetadataDTO;
import com.algoarena.model.QuestionLevel;
import com.algoarena.model.Solution;
import com.algoarena.model.Question;
import com.algoarena.model.User;
import com.algoarena.model.UserProgress;
import com.algoarena.model.Category;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.repository.ApproachRepository;
import com.algoarena.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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
    private UserProgressRepository userProgressRepository;

    @Autowired
    private SolutionService solutionService;

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private BulkApproachService bulkApproachService;

    // ==================== HYBRID CACHING METHODS ====================

    /**
     * HYBRID: Get questions summary with user progress - CACHED with smart eviction
     * Cache key includes all filter parameters for proper cache segmentation
     */
    @Cacheable(value = "questionsSummary", key = "#userId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_cat_' + (#categoryId ?: 'all') + '_lvl_' + (#level ?: 'all') + '_search_' + (#search ?: 'none')")
    public Page<QuestionSummaryDTO> getQuestionsWithProgress(
            Pageable pageable,
            String categoryId,
            String level,
            String search,
            String userId) {

        // System.out.println("CACHE MISS: Fetching fresh questions data for user: " +
        // userId);

        // Step 1: Get questions with filtering
        Page<Question> questionsPage = getAllQuestionsFiltered(pageable, categoryId, level, search);

        // Step 2: Get all question IDs from the page
        List<String> questionIds = questionsPage.getContent()
                .stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        if (questionIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Step 3: Get user progress for these questions (optimized)
        Map<String, UserProgress> progressMap = new HashMap<>();
        for (String questionId : questionIds) {
            Optional<UserProgress> progress = userProgressRepository.findByUser_IdAndQuestion_Id(userId, questionId);
            if (progress.isPresent()) {
                progressMap.put(questionId, progress.get());
            }
        }

        // System.out.println(
        // "DEBUG: Found " + progressMap.size() + " progress records out of " +
        // questionIds.size() + " questions");

        // Step 4: TRULY BULK - Get approach counts using single aggregation query
        // System.out.println("DEBUG: Using bulk approach count service for " +
        // questionIds.size() + " questions for user: " + userId);

        Map<String, Integer> approachCountMap = bulkApproachService.getBulkApproachCounts(userId, questionIds);

        // Step 5: Convert to QuestionSummaryDTO with embedded user progress and
        // approach counts
        List<QuestionSummaryDTO> summaryList = questionsPage.getContent()
                .stream()
                .map(question -> {
                    QuestionSummaryDTO summary = new QuestionSummaryDTO(
                            question.getId(),
                            question.getTitle(),
                            question.getCategory().getId(),
                            question.getCategory().getName(),
                            question.getLevel(),
                            question.getCreatedAt());

                    // Add user progress
                    UserProgress progress = progressMap.get(question.getId());
                    int approachCount = approachCountMap.getOrDefault(question.getId(), 0);

                    if (progress != null && progress.isSolved()) {
                        summary.setUserProgress(new QuestionSummaryDTO.UserProgressSummary(
                                true,
                                progress.getSolvedAt(),
                                approachCount));
                    } else {
                        summary.setUserProgress(new QuestionSummaryDTO.UserProgressSummary(
                                false,
                                null,
                                approachCount));
                    }

                    return summary;
                })
                .collect(Collectors.toList());

        // Return paginated result
        return new PageImpl<>(summaryList, pageable, questionsPage.getTotalElements());
    }

    /**
     * Helper method to get filtered questions (can be cached separately)
     */
    @Cacheable(value = "questionsList", key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_cat_' + (#categoryId ?: 'all') + '_lvl_' + (#level ?: 'all') + '_search_' + (#search ?: 'none')")
    public Page<Question> getAllQuestionsFiltered(Pageable pageable, String categoryId, String level, String search) {
        // System.out.println("CACHE MISS: Fetching filtered questions from database");

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

    // ==================== CRUD OPERATIONS WITH PROPER CACHE EVICTION
    // ====================

    /**
     * Create a new question
     * UPDATED: Now handles displayOrder field
     */
    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary", "questionsMetadata" }, allEntries = true)
    @Transactional
    public QuestionDTO createQuestion(QuestionDTO questionDTO, User currentUser) {
        // Check if question with same title already exists
        if (questionRepository.existsByTitleIgnoreCase(questionDTO.getTitle())) {
            throw new RuntimeException("Question with this title already exists");
        }

        Question question = new Question();
        question.setTitle(questionDTO.getTitle());
        question.setStatement(questionDTO.getStatement());
        question.setImageUrls(questionDTO.getImageUrls());
        question.setImageFolderUrl(questionDTO.getImageFolderUrl());

        // Convert code snippets
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

        // Set category
        Category category = categoryRepository.findById(questionDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        question.setLevel(questionDTO.getLevel());

        // NEW: Handle displayOrder - if not provided, auto-assign based on max existing
        // order
        if (questionDTO.getDisplayOrder() != null) {
            question.setDisplayOrder(questionDTO.getDisplayOrder());
        } else {
            // Auto-assign display order based on existing questions in same category/level
            Integer maxOrder = questionRepository.findMaxDisplayOrderByCategoryAndLevel(
                    category.getId(), questionDTO.getLevel());
            question.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 1);
        }

        question.setCreatedBy(currentUser);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        Question savedQuestion = questionRepository.save(question);

        System.out.println("✓ Created new question: " + savedQuestion.getTitle() +
                " with displayOrder: " + savedQuestion.getDisplayOrder());

        return QuestionDTO.fromEntity(savedQuestion);
    }

    /**
     * Update an existing question
     * UPDATED: Now handles displayOrder field
     */
    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary", "questionsMetadata",
            "adminQuestionDetail" }, allEntries = true)
    @Transactional
    public QuestionDTO updateQuestion(String id, QuestionDTO questionDTO) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));

        // Check if changing title to an existing one
        if (!question.getTitle().equals(questionDTO.getTitle()) &&
                questionRepository.existsByTitleIgnoreCase(questionDTO.getTitle())) {
            throw new RuntimeException("Question with this title already exists");
        }

        question.setTitle(questionDTO.getTitle());
        question.setStatement(questionDTO.getStatement());
        question.setImageUrls(questionDTO.getImageUrls());
        question.setImageFolderUrl(questionDTO.getImageFolderUrl());

        // Convert code snippets
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

        // Update category if changed
        if (!question.getCategory().getId().equals(questionDTO.getCategoryId())) {
            Category category = categoryRepository.findById(questionDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            question.setCategory(category);
        }

        question.setLevel(questionDTO.getLevel());

        // NEW: Update displayOrder if provided
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
     * Delete a question
     * UPDATED: Includes cache eviction for metadata
     */
    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary", "questionsMetadata",
            "adminQuestionDetail" }, allEntries = true)
    @Transactional
    public void deleteQuestion(String id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Question not found with id: " + id);
        }

        // Delete related solutions first
        solutionRepository.deleteByQuestion_Id(id);

        // Delete related approaches
        approachRepository.deleteByQuestion_Id(id);

        // Delete user progress
        userProgressRepository.deleteByQuestion_Id(id);

        // Delete the question
        questionRepository.deleteById(id);

        // System.out.println("✓ Deleted question with id: " + id + " and all related
        // data");
    }

    // ==================== EXISTING METHODS ====================

    public Page<QuestionDTO> getAllQuestions(Pageable pageable, String categoryId, String level, String search) {
        Page<Question> questions = getAllQuestionsFiltered(pageable, categoryId, level, search);
        return questions.map(QuestionDTO::fromEntity);
    }

    public QuestionDetailDTO getQuestionDetails(String questionId, String userId) {
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            return null;
        }

        QuestionDTO questionDTO = QuestionDTO.fromEntity(question);
        List<SolutionDTO> solutions = solutionService.getSolutionsByQuestion(questionId);

        var userProgress = userProgressService.getProgressByQuestionAndUser(questionId, userId);
        boolean solved = userProgress != null ? userProgress.isSolved() : false;
        var solvedAt = userProgress != null ? userProgress.getSolvedAt() : null;

        return new QuestionDetailDTO(questionDTO, solutions, solved, solvedAt);
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
        // System.out.println("CACHE MISS: Fetching question counts from database");

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

    /**
     * Update display order for a single question
     * Evicts relevant caches after update
     */
    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary" }, allEntries = true)
    @Transactional
    public void updateQuestionDisplayOrder(String questionId, Integer displayOrder) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        question.setDisplayOrder(displayOrder);
        questionRepository.save(question);

        System.out.println("✓ Updated displayOrder to " + displayOrder +
                " for question: " + question.getTitle());
    }

    /**
     * Batch update display order for multiple questions
     * Used for drag-and-drop reordering
     * 
     * @param updates List of maps containing questionId and displayOrder
     * @return Number of questions updated
     */
    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary" }, allEntries = true)
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

    /**
     * Get questions by category and level for ordering interface
     * Returns minimal data needed for reordering
     */
    public List<Map<String, Object>> getQuestionsByCategoryAndLevelForOrdering(
            String categoryId, String levelStr) {

        QuestionLevel level = QuestionLevel.valueOf(levelStr.toUpperCase());
        List<Question> questions = questionRepository.findByCategory_IdAndLevel(categoryId, level);

        // Sort by displayOrder (nulls last)
        questions.sort(Comparator.comparing(
                q -> q.getDisplayOrder() != null ? q.getDisplayOrder() : Integer.MAX_VALUE));

        // Return minimal data
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

    /**
     * Reset display order for all questions in a category and level
     * Re-numbers them sequentially (1, 2, 3, ...) based on current displayOrder
     */
    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary" }, allEntries = true)
    @Transactional
    public int resetDisplayOrder(String categoryId, String levelStr) {
        QuestionLevel level = QuestionLevel.valueOf(levelStr.toUpperCase());
        List<Question> questions = questionRepository.findByCategory_IdAndLevel(categoryId, level);

        // Sort by current displayOrder (nulls last), then by createdAt
        questions.sort(Comparator
                .comparing((Question q) -> q.getDisplayOrder() != null ? q.getDisplayOrder() : Integer.MAX_VALUE)
                .thenComparing(Question::getCreatedAt));

        // Re-assign sequential display orders
        int order = 1;
        for (Question question : questions) {
            question.setDisplayOrder(order++);
            questionRepository.save(question);
        }

        System.out.println("✓ Reset displayOrder for " + questions.size() +
                " questions in category " + categoryId + " - " + level);

        return questions.size();
    }

    /**
     * Get admin questions summary (lightweight, cached)
     * Returns paginated summary without full content
     * UPDATED: Includes displayOrder, removes approachCount and solvedByCount
     */
    @Cacheable(value = "adminQuestionsSummary", key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<AdminQuestionSummaryDTO> getAdminQuestionsSummary(Pageable pageable) {
        System.out.println("CACHE MISS: Fetching admin questions summary from database");

        // Fetch questions ordered by createdAt desc (latest first)
        Page<Question> questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable);

        return questions.map(question -> {
            AdminQuestionSummaryDTO dto = new AdminQuestionSummaryDTO();
            dto.setId(question.getId());
            dto.setTitle(question.getTitle());
            dto.setLevel(question.getLevel());
            dto.setCategoryName(question.getCategory() != null ? question.getCategory().getName() : "Unknown");
            dto.setDisplayOrder(question.getDisplayOrder()); // NEW: Include display order
            dto.setImageCount(question.getImageUrls() != null ? question.getImageUrls().size() : 0);
            dto.setHasCodeSnippets(question.getCodeSnippets() != null && !question.getCodeSnippets().isEmpty());
            dto.setCreatedByName(question.getCreatedBy() != null ? question.getCreatedBy().getName() : "Unknown");
            dto.setUpdatedAt(question.getUpdatedAt());

            // Fetch solution count only
            dto.setSolutionCount((int) solutionRepository.countByQuestion_Id(question.getId()));

            // REMOVED: approachCount and solvedByCount

            return dto;
        });
    }

    /**
     * Get complete question details for admin
     * NEW METHOD: Returns full question content for admin editing
     * 
     * @param questionId Question ID
     * @return Complete QuestionDetailDTO with all content
     */
    @Cacheable(value = "adminQuestionDetail", key = "#questionId")
    public QuestionDetailDTO getAdminQuestionById(String questionId) {
        System.out.println("CACHE MISS: Fetching admin question detail from database");

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        QuestionDetailDTO detailDTO = new QuestionDetailDTO();

        // Question details
        detailDTO.setQuestion(QuestionDTO.fromEntity(question));

        // Add display order to the question DTO
        if (detailDTO.getQuestion() != null) {
            // Note: You may need to add displayOrder field to QuestionDTO if not present
            detailDTO.getQuestion().setDisplayOrder(question.getDisplayOrder());
        }

        // Fetch all solutions for this question
        List<Solution> solutions = solutionRepository.findByQuestion_Id(questionId);
        List<SolutionDTO> solutionDTOs = solutions.stream()
                .map(SolutionDTO::fromEntity)
                .collect(Collectors.toList());
        detailDTO.setSolutions(solutionDTOs);

        return detailDTO;
    }

    /**
     * Get questions metadata (lightweight, cached globally)
     * UPDATED: Now includes category name for each question
     * Endpoint changed from /api/user/questions/metadata to /api/questions/metadata
     */
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
                    categoryName // NEW: Include category name
            );
            metadataMap.put(question.getId(), metadata);
        }

        QuestionsMetadataDTO result = new QuestionsMetadataDTO();
        result.setQuestions(metadataMap);

        return result;
    }
}