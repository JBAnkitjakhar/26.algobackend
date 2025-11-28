// src/main/java/com/algoarena/service/dsa/QuestionService.java
package com.algoarena.service.dsa;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;

import com.algoarena.dto.dsa.AdminQuestionSummaryDTO;
import com.algoarena.dto.dsa.QuestionDTO;
import com.algoarena.dto.user.QuestionsMetadataDTO;
import com.algoarena.model.QuestionLevel;
import com.algoarena.model.Solution;
import com.algoarena.model.Question;
import com.algoarena.model.User;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.service.file.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
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
    private SolutionService solutionService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ApproachService approachService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary", "questionsMetadata" }, allEntries = true)
    @Transactional
    public QuestionDTO createQuestion(QuestionDTO questionDTO, User currentUser) {
        if (questionRepository.existsByTitleIgnoreCase(questionDTO.getTitle())) {
            throw new RuntimeException("Question with this title already exists");
        }

        if (!categoryRepository.existsById(questionDTO.getCategoryId())) {
            throw new RuntimeException("Category not found with id: " + questionDTO.getCategoryId());
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

        question.setCategoryId(questionDTO.getCategoryId());
        question.setLevel(questionDTO.getLevel());

        if (questionDTO.getDisplayOrder() != null) {
            question.setDisplayOrder(questionDTO.getDisplayOrder());
        } else {
            Integer maxOrder = questionRepository.findTop1ByCategoryIdAndLevelOrderByDisplayOrderDesc(
                    questionDTO.getCategoryId(),
                    questionDTO.getLevel())
                    .map(Question::getDisplayOrder)
                    .orElse(0);
            question.setDisplayOrder(maxOrder + 1);
        }

        question.setCreatedById(currentUser.getId());
        question.setCreatedByName(currentUser.getName());
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        Question savedQuestion = questionRepository.save(question);

        categoryService.addQuestionToCategory(
                savedQuestion.getCategoryId(),
                savedQuestion.getId(),
                savedQuestion.getLevel());

        System.out.println("✓ Created question: " + savedQuestion.getTitle());

        return QuestionDTO.fromEntity(savedQuestion);
    }

    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary", "questionsMetadata",
            "questionDetail" }, allEntries = true)
    @Transactional
    public QuestionDTO updateQuestion(String id, QuestionDTO questionDTO) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));

        if (!question.getTitle().equals(questionDTO.getTitle()) &&
                questionRepository.existsByTitleIgnoreCase(questionDTO.getTitle())) {
            throw new RuntimeException("Question with this title already exists");
        }

        if (!question.getCategoryId().equals(questionDTO.getCategoryId())) {
            if (!categoryRepository.existsById(questionDTO.getCategoryId())) {
                throw new RuntimeException("Category not found with id: " + questionDTO.getCategoryId());
            }
        }

        if (questionDTO.getVersion() != null && !questionDTO.getVersion().equals(question.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Question was modified by another user. Please refresh and try again.");
        }

        String oldCategoryId = question.getCategoryId();
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

        boolean categoryChanged = !oldCategoryId.equals(questionDTO.getCategoryId());
        boolean levelChanged = oldLevel != questionDTO.getLevel();

        if (categoryChanged || levelChanged) {
            question.setCategoryId(questionDTO.getCategoryId());
            question.setLevel(questionDTO.getLevel());

            categoryService.moveQuestion(
                    oldCategoryId,
                    question.getCategoryId(),
                    question.getId(),
                    oldLevel,
                    question.getLevel());
        }

        if (questionDTO.getDisplayOrder() != null) {
            question.setDisplayOrder(questionDTO.getDisplayOrder());
        }

        question.setUpdatedAt(LocalDateTime.now());

        Question updatedQuestion = questionRepository.save(question);

        System.out.println("✓ Updated question: " + updatedQuestion.getTitle());

        return QuestionDTO.fromEntity(updatedQuestion);
    }

    // adminQuestionsSummary,\
    // adminSolutionsSummary,\
    // globalCategories,\
    // userMeStats,\
    // questionsMetadata,\
    // questionDetail,\
    // questionSolutions,\
    // solutionDetail,\
    // courseTopic,\
    // courseDocsList,\
    // courseDoc,\
    // topicNamesPublic,\
    // topicNamesAdmin

    @CacheEvict(value = { "globalCategories", "adminQuestionsSummary", "questionsMetadata",
            "questionDetail", "userMeStats", "adminSolutionsSummary", "solutionDetail",
            "questionSolutions" }, allEntries = true)
    @Transactional
    public void deleteQuestion(String id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));

        // ✅ STEP 1: Delete all solutions (with their images and visualizers)
        List<Solution> solutions = solutionRepository.findByQuestionId(id);
        System.out.println("Deleting " + solutions.size() + " solutions for question...");

        for (Solution solution : solutions) {
            // This will now properly delete solution images and visualizers
            solutionService.deleteSolution(solution.getId());
        }

        // ✅ STEP 2: Delete question's own images
        if (question.getImageUrls() != null && !question.getImageUrls().isEmpty()) {
            System.out.println("Deleting " + question.getImageUrls().size() + " images from question...");

            for (String imageUrl : question.getImageUrls()) {
                try {
                    String publicId = extractPublicIdFromUrl(imageUrl);
                    cloudinaryService.deleteImage(publicId);
                    System.out.println("  ✓ Deleted image: " + publicId);
                } catch (Exception e) {
                    System.err.println("  ✗ Failed to delete image: " + e.getMessage());
                }
            }
        }

        // ✅ STEP 3: Remove from category
        categoryService.removeQuestionFromCategory(
                question.getCategoryId(),
                question.getId(),
                question.getLevel());

        // ✅ STEP 4: Delete approaches
        approachService.deleteAllApproachesForQuestion(id);

        // ✅ STEP 5: Remove from user progress
        int removedFromUsers = userProgressService.removeQuestionFromAllUsers(id);
        System.out.println("✓ Removed from " + removedFromUsers + " users' progress");

        // ✅ STEP 6: Delete question from database
        questionRepository.deleteById(id);
        System.out.println("✓ Deleted question: " + question.getTitle());
    }

    /**
     * Helper method to extract Cloudinary public ID from URL
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            throw new IllegalArgumentException("Invalid Cloudinary URL");
        }

        try {
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                throw new IllegalArgumentException("Invalid Cloudinary URL format");
            }

            String afterUpload = imageUrl.substring(uploadIndex + 8);

            if (afterUpload.startsWith("v") && afterUpload.indexOf("/") > 0) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex > 0) {
                return afterUpload.substring(0, dotIndex);
            }

            return afterUpload;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract public ID: " + e.getMessage());
        }
    }

    public boolean existsById(String id) {
        return questionRepository.existsById(id);
    }

    public boolean existsByTitle(String title) {
        return questionRepository.existsByTitleIgnoreCase(title);
    }

    @Cacheable(value = "adminQuestionsSummary", key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<AdminQuestionSummaryDTO> getAdminQuestionsSummary(Pageable pageable) {
        System.out.println("CACHE MISS: Fetching admin questions summary from database");

        Page<Question> questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable);

        // OPTIMIZATION: Fetch all solution counts in ONE query
        List<String> questionIds = questions.getContent().stream()
                .map(Question::getId)
                .toList();

        Map<String, Integer> solutionCounts = getSolutionCountsForQuestions(questionIds);

        return questions.map(question -> {
            AdminQuestionSummaryDTO dto = new AdminQuestionSummaryDTO();
            dto.setId(question.getId());
            dto.setTitle(question.getTitle());
            dto.setLevel(question.getLevel());
            dto.setCategoryId(question.getCategoryId());
            dto.setDisplayOrder(question.getDisplayOrder());
            dto.setImageCount(question.getImageUrls() != null ? question.getImageUrls().size() : 0);
            dto.setHasCodeSnippets(question.getCodeSnippets() != null && !question.getCodeSnippets().isEmpty());
            dto.setCreatedByName(question.getCreatedByName());
            dto.setUpdatedAt(question.getUpdatedAt());
            dto.setSolutionCount(solutionCounts.getOrDefault(question.getId(), 0)); // FIXED

            return dto;
        });
    }

    /**
     * Fetch solution counts for multiple questions in a single aggregation query
     * 
     * MongoDB Aggregation Pipeline:
     * 1. Match: Filter solutions where questionId is in the provided list
     * 2. Group: Group by questionId and count documents in each group
     * 
     * Example:
     * Input: ["id1", "id2", "id3"]
     * Output: {"id1": 5, "id2": 3, "id3": 0}
     * 
     * This reduces 20 separate count queries to 1 aggregation query
     */
    private Map<String, Integer> getSolutionCountsForQuestions(List<String> questionIds) {
        if (questionIds.isEmpty()) {
            return new HashMap<>();
        }

        // MongoDB Aggregation Pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                // Stage 1: Match solutions where questionId is in our list
                Aggregation.match(Criteria.where("questionId").in(questionIds)),
                // Stage 2: Group by questionId and count
                Aggregation.group("questionId").count().as("count"));

        // Execute aggregation
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(
                aggregation,
                "solutions", // collection name
                org.bson.Document.class);

        // Convert results to Map
        Map<String, Integer> counts = new HashMap<>();
        for (org.bson.Document doc : results.getMappedResults()) {
            String questionId = doc.getString("_id"); // _id is the grouped field (questionId)
            Integer count = doc.getInteger("count"); // FIXED: use getInteger instead of getLong
            counts.put(questionId, count);
        }

        return counts;
    }

    /**
     * Get question by ID for authenticated users
     * Globally cached
     */
    @Cacheable(value = "questionDetail", key = "#questionId")
    public QuestionDTO getQuestionById(String questionId) {
        System.out.println("CACHE MISS: Fetching question detail for: " + questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        return QuestionDTO.fromEntity(question);
    }

    @Cacheable(value = "questionsMetadata")
    public QuestionsMetadataDTO getQuestionsMetadata() {
        System.out.println("CACHE MISS: Fetching questions metadata");

        List<Question> allQuestions = questionRepository.findAll();
        Map<String, QuestionsMetadataDTO.QuestionMetadata> metadataMap = new HashMap<>();

        for (Question question : allQuestions) {
            QuestionsMetadataDTO.QuestionMetadata metadata = new QuestionsMetadataDTO.QuestionMetadata(
                    question.getId(),
                    question.getTitle(),
                    question.getLevel(),
                    question.getCategoryId());
            metadataMap.put(question.getId(), metadata);
        }

        QuestionsMetadataDTO result = new QuestionsMetadataDTO();
        result.setQuestions(metadataMap);

        return result;
    }
}