// src/main/java/com/algoarena/service/dsa/ApproachService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.ApproachDetailDTO;
import com.algoarena.dto.dsa.ApproachMetadataDTO;
import com.algoarena.model.Question;
import com.algoarena.model.User;
import com.algoarena.model.UserApproaches;
import com.algoarena.model.UserApproaches.ApproachData;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.UserApproachesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApproachService {

    @Autowired
    private UserApproachesRepository userApproachesRepository;

    @Autowired
    private QuestionRepository questionRepository;

    /**
     * GET /api/approaches/question/{questionId}
     * Get CURRENT USER's approaches metadata for a question
     */
    public List<ApproachMetadataDTO> getMyApproachesForQuestion(String userId, String questionId) {
        Optional<UserApproaches> userApproachesOpt = userApproachesRepository.findByUserId(userId);
        
        if (userApproachesOpt.isEmpty()) {
            return new ArrayList<>();
        }

        UserApproaches userApproaches = userApproachesOpt.get();
        List<ApproachData> approaches = userApproaches.getApproachesForQuestion(questionId);

        return approaches.stream()
                .map(data -> new ApproachMetadataDTO(data, userId, userApproaches.getUserName()))
                .collect(Collectors.toList());
    }

    /**
     * GET /api/approaches/question/{questionId}/{approachId}
     * Get full content for CURRENT USER's specific approach
     */
    public ApproachDetailDTO getMyApproachDetail(String userId, String questionId, String approachId) {
        UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No approaches found"));

        ApproachData approach = userApproaches.findApproachById(approachId);
        
        if (approach == null) {
            throw new RuntimeException("Approach not found");
        }

        // Verify approach belongs to the specified question
        if (!approach.getQuestionId().equals(questionId)) {
            throw new RuntimeException("Approach does not belong to this question");
        }

        return new ApproachDetailDTO(approach, userId, userApproaches.getUserName());
    }

    /**
     * POST /api/approaches/question/{questionId}
     * Create new approach for CURRENT USER
     */
    public ApproachDetailDTO createApproach(String userId, String questionId, 
                                           ApproachDetailDTO dto, User user) {
        // Validate question exists
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Get or create user's approaches document
        UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                .orElse(new UserApproaches(userId, user.getName()));

        // Create new approach data
        ApproachData newApproach = new ApproachData(questionId, question.getTitle(), dto.getTextContent());
        newApproach.setCodeContent(dto.getCodeContent());
        newApproach.setCodeLanguage(dto.getCodeLanguage() != null ? dto.getCodeLanguage() : "java");

        // Add approach (validates 3-approach limit and 15KB combined size)
        userApproaches.addApproach(questionId, newApproach);

        // Save document
        userApproachesRepository.save(userApproaches);

        System.out.println("✓ Created approach for user: " + user.getName() + 
                         " on question: " + question.getTitle());

        return new ApproachDetailDTO(newApproach, userId, user.getName());
    }

    /**
     * PUT /api/approaches/question/{questionId}/{approachId}
     * Update CURRENT USER's approach
     */
    public ApproachDetailDTO updateApproach(String userId, String questionId, 
                                           String approachId, ApproachDetailDTO dto) {
        UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No approaches found"));

        // Verify approach exists and belongs to question
        ApproachData approach = userApproaches.findApproachById(approachId);
        if (approach == null) {
            throw new RuntimeException("Approach not found");
        }
        if (!approach.getQuestionId().equals(questionId)) {
            throw new RuntimeException("Approach does not belong to this question");
        }

        // Update approach (validates size limits)
        userApproaches.updateApproach(
            approachId, 
            dto.getTextContent(), 
            dto.getCodeContent(), 
            dto.getCodeLanguage()
        );

        // Save document
        userApproachesRepository.save(userApproaches);

        System.out.println("✓ Updated approach: " + approachId);

        return new ApproachDetailDTO(approach, userId, userApproaches.getUserName());
    }

    /**
     * DELETE /api/approaches/question/{questionId}/{approachId}
     * Delete CURRENT USER's approach
     */
    public void deleteApproach(String userId, String questionId, String approachId) {
        UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No approaches found"));

        // Verify approach exists and belongs to question
        ApproachData approach = userApproaches.findApproachById(approachId);
        if (approach == null) {
            throw new RuntimeException("Approach not found");
        }
        if (!approach.getQuestionId().equals(questionId)) {
            throw new RuntimeException("Approach does not belong to this question");
        }

        // Remove approach
        userApproaches.removeApproach(questionId, approachId);

        // Save document (or delete if empty)
        if (userApproaches.getTotalApproaches() == 0) {
            userApproachesRepository.delete(userApproaches);
            System.out.println("✓ Deleted approach and removed empty document: " + approachId);
        } else {
            userApproachesRepository.save(userApproaches);
            System.out.println("✓ Deleted approach: " + approachId);
        }
    }

    /**
     * GET /api/approaches/my-approaches
     * Get all approaches by CURRENT USER (for profile page)
     */
    public List<ApproachMetadataDTO> getMyAllApproaches(String userId) {
        UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                .orElse(null);

        if (userApproaches == null) {
            return new ArrayList<>();
        }

        return userApproaches.getAllApproachesFlat().stream()
                .map(data -> new ApproachMetadataDTO(data, userId, userApproaches.getUserName()))
                .collect(Collectors.toList());
    }

    /**
     * Get size/count usage for CURRENT USER on a specific question
     * Used to show remaining space in UI
     */
    public Map<String, Object> getMyQuestionUsage(String userId, String questionId) {
        UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                .orElse(null);

        Map<String, Object> usage = new HashMap<>();

        if (userApproaches == null) {
            usage.put("usedBytes", 0);
            usage.put("usedKB", 0.0);
            usage.put("remainingBytes", UserApproaches.MAX_COMBINED_SIZE_PER_QUESTION_BYTES);
            usage.put("remainingKB", 15.0);
            usage.put("approachCount", 0);
            usage.put("remainingSlots", 3);
            return usage;
        }

        int totalSize = userApproaches.getTotalSizeForQuestion(questionId);
        int remaining = userApproaches.getRemainingBytesForQuestion(questionId);
        int count = userApproaches.getApproachCountForQuestion(questionId);

        usage.put("usedBytes", totalSize);
        usage.put("usedKB", totalSize / 1024.0);
        usage.put("remainingBytes", remaining);
        usage.put("remainingKB", remaining / 1024.0);
        usage.put("approachCount", count);
        usage.put("remainingSlots", UserApproaches.MAX_APPROACHES_PER_QUESTION - count);
        usage.put("maxBytes", UserApproaches.MAX_COMBINED_SIZE_PER_QUESTION_BYTES);
        usage.put("maxKB", 15.0);

        return usage;
    }

    /**
     * Admin: Delete all approaches for a question (when question is deleted)
     * This is the ONLY admin function needed
     */
    public void deleteAllApproachesForQuestion(String questionId) {
        List<UserApproaches> allUsers = userApproachesRepository.findAll();
        
        int deletedCount = 0;
        for (UserApproaches userApproaches : allUsers) {
            List<ApproachData> approaches = userApproaches.getApproachesForQuestion(questionId);
            
            if (!approaches.isEmpty()) {
                // Remove all approaches for this question
                for (ApproachData approach : new ArrayList<>(approaches)) {
                    userApproaches.removeApproach(questionId, approach.getId());
                    deletedCount++;
                }
                
                // Save or delete document
                if (userApproaches.getTotalApproaches() == 0) {
                    userApproachesRepository.delete(userApproaches);
                } else {
                    userApproachesRepository.save(userApproaches);
                }
            }
        }
        
        System.out.println("✓ Deleted " + deletedCount + " approaches for question: " + questionId);
    }
}