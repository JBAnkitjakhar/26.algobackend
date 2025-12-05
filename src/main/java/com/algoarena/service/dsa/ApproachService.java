// src/main/java/com/algoarena/service/dsa/ApproachService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.ApproachDetailDTO;
import com.algoarena.dto.dsa.ApproachMetadataDTO;
import com.algoarena.exception.ConcurrentModificationException;
import com.algoarena.model.ProgrammingLanguage;
import com.algoarena.model.User;
import com.algoarena.model.UserApproaches;
import com.algoarena.model.UserApproaches.ApproachData;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.UserApproachesRepository;
import com.algoarena.util.HtmlSanitizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
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

    @Autowired
    private HtmlSanitizer htmlSanitizer;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    // used
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

    // used
    public ApproachDetailDTO getMyApproachDetail(String userId, String questionId, String approachId) {
        UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No approaches found"));

        ApproachData approach = userApproaches.findApproachById(approachId);

        if (approach == null) {
            throw new RuntimeException("Approach not found");
        }

        if (!approach.getQuestionId().equals(questionId)) {
            throw new RuntimeException("Approach does not belong to this question");
        }

        return new ApproachDetailDTO(approach, userId, userApproaches.getUserName());
    }

    // used
    public ApproachDetailDTO createApproach(String userId, String questionId,
            ApproachDetailDTO dto, User user) {

        // ✅ Validate question exists (but don't need to store it)
        questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // ✅ Validate lengths
        if (dto.getCodeContent() == null || dto.getCodeContent().trim().isEmpty()) {
            throw new RuntimeException("Code content is required");
        }
        if (dto.getCodeContent().length() > 50000) {
            throw new RuntimeException("Code content exceeds 50,000 characters");
        }
        if (dto.getTextContent() == null || dto.getTextContent().trim().isEmpty()) {
            throw new RuntimeException("Text content is required");
        }
        if (dto.getTextContent().length() > 10000) {
            throw new RuntimeException("Text content exceeds 10,000 characters");
        }

        int attempt = 0;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                        .orElse(new UserApproaches(userId, user.getName()));

                // Validate and normalize language
                String normalizedLanguage = "java";
                if (dto.getCodeLanguage() != null && !dto.getCodeLanguage().trim().isEmpty()) {
                    try {
                        ProgrammingLanguage.fromString(dto.getCodeLanguage());
                        normalizedLanguage = dto.getCodeLanguage().toLowerCase().trim();
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid programming language: " + dto.getCodeLanguage() +
                                ". Allowed: java, python, javascript, cpp, c, csharp, go, rust, kotlin, swift, ruby, php, typescript");
                    }
                }

                // ✅ Sanitize text, store code as-is
                String safeText = htmlSanitizer.sanitizeText(dto.getTextContent());
                String safeCode = dto.getCodeContent(); // Store raw code

                ApproachData newApproach = new ApproachData(questionId, safeText);
                newApproach.setCodeContent(safeCode);
                newApproach.setCodeLanguage(normalizedLanguage);

                userApproaches.addApproach(questionId, newApproach);
                userApproachesRepository.save(userApproaches);

                // System.out.println("✓ Created approach for user: " + user.getName());

                return new ApproachDetailDTO(newApproach, userId, user.getName());

            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    throw new ConcurrentModificationException();
                }
                try {
                    Thread.sleep(100 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ConcurrentModificationException();
                }
            }
        }

        throw new ConcurrentModificationException();
    }

    //used
    public ApproachDetailDTO updateApproach(String userId, String questionId,
            String approachId, ApproachDetailDTO dto) {

        // ✅ Validate lengths
        if (dto.getCodeContent() != null && dto.getCodeContent().length() > 50000) {
            throw new RuntimeException("Code content exceeds 50,000 characters");
        }
        if (dto.getTextContent() != null && dto.getTextContent().length() > 10000) {
            throw new RuntimeException("Text content exceeds 10,000 characters");
        }

        int attempt = 0;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("No approaches found"));

                ApproachData approach = userApproaches.findApproachById(approachId);
                if (approach == null) {
                    throw new RuntimeException("Approach not found");
                }
                if (!approach.getQuestionId().equals(questionId)) {
                    throw new RuntimeException("Approach does not belong to this question");
                }

                // ✅ Sanitize text, store code as-is
                String safeText = dto.getTextContent() != null
                        ? htmlSanitizer.sanitizeText(dto.getTextContent())
                        : null;
                String safeCode = dto.getCodeContent(); // Store raw code

                // Validate and normalize language
                String safeLanguage = null;
                if (dto.getCodeLanguage() != null && !dto.getCodeLanguage().trim().isEmpty()) {
                    try {
                        ProgrammingLanguage.fromString(dto.getCodeLanguage());
                        safeLanguage = dto.getCodeLanguage().toLowerCase().trim();
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid programming language: " + dto.getCodeLanguage() +
                                ". Allowed: java, python, javascript, cpp, c, csharp, go, rust, kotlin, swift, ruby, php, typescript");
                    }
                }

                userApproaches.updateApproach(approachId, safeText, safeCode, safeLanguage);
                userApproachesRepository.save(userApproaches);

                // System.out.println("✓ Updated approach: " + approachId);

                return new ApproachDetailDTO(approach, userId, userApproaches.getUserName());

            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    throw new ConcurrentModificationException();
                }
                try {
                    Thread.sleep(100 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ConcurrentModificationException();
                }
            }
        }

        throw new ConcurrentModificationException();
    }

    // used
    public void deleteApproach(String userId, String questionId, String approachId) {
        int attempt = 0;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("No approaches found"));

                ApproachData approach = userApproaches.findApproachById(approachId);
                if (approach == null) {
                    throw new RuntimeException("Approach not found");
                }
                if (!approach.getQuestionId().equals(questionId)) {
                    throw new RuntimeException("Approach does not belong to this question");
                }

                userApproaches.removeApproach(questionId, approachId);

                if (userApproaches.getTotalApproaches() == 0) {
                    userApproachesRepository.delete(userApproaches);
                    // System.out.println("✓ Deleted approach and removed empty document: " + approachId);
                } else {
                    userApproachesRepository.save(userApproaches);
                    // System.out.println("✓ Deleted approach: " + approachId);
                }

                return;

            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    throw new ConcurrentModificationException();
                }
                try {
                    Thread.sleep(100 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ConcurrentModificationException();
                }
            }
        }

        throw new ConcurrentModificationException();
    }

    // used
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

    public void deleteAllApproachesForQuestion(String questionId) {
        List<UserApproaches> allUsers = userApproachesRepository.findAll();

        int deletedCount = 0;
        for (UserApproaches userApproaches : allUsers) {
            List<ApproachData> approaches = userApproaches.getApproachesForQuestion(questionId);

            if (!approaches.isEmpty()) {
                for (ApproachData approach : new ArrayList<>(approaches)) {
                    userApproaches.removeApproach(questionId, approach.getId());
                    deletedCount++;
                }

                if (userApproaches.getTotalApproaches() == 0) {
                    userApproachesRepository.delete(userApproaches);
                } else {
                    userApproachesRepository.save(userApproaches);
                }
            }
        }

        System.out.println("✓ Deleted " + deletedCount + " approaches for question: " + questionId);
    }

    // used by admins
    public void deleteAllApproachesByUserForQuestion(String userId, String questionId) {
        UserApproaches userApproaches = userApproachesRepository.findByUserId(userId)
                .orElse(null);

        if (userApproaches == null) {
            return; // No approaches to delete
        }

        List<ApproachData> approaches = userApproaches.getApproachesForQuestion(questionId);

        for (ApproachData approach : new ArrayList<>(approaches)) {
            userApproaches.removeApproach(questionId, approach.getId());
        }

        if (userApproaches.getTotalApproaches() == 0) {
            userApproachesRepository.delete(userApproaches);
        } else {
            userApproachesRepository.save(userApproaches);
        }

        // System.out.println("✓ Deleted " + approaches.size() + " approaches by user " +
        //         userId + " for question: " + questionId);
    }
}