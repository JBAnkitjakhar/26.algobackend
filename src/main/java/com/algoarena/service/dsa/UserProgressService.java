// File: src/main/java/com/algoarena/service/dsa/UserProgressService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.user.UserMeStatsDTO;
import com.algoarena.dto.user.QuestionSolveStatusDTO;
import com.algoarena.exception.*;
import com.algoarena.model.UserProgress;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.UserProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserProgressService {

    private static final Logger logger = LoggerFactory.getLogger(UserProgressService.class);
    private static final int MAX_RETRIES = 3;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private QuestionRepository questionRepository;

    /**
     * Validate question ID
     */
    private void validateQuestionId(String questionId) {
        if (questionId == null || questionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Question ID is required");
        }
    }

    /**
     * Get user stats - cached by userId
     * Rate limiting handled by RateLimitInterceptor (60/min for reads)
     */
    @Cacheable(value = "userMeStats", key = "#userId")
    public UserMeStatsDTO getUserMeStats(String userId) {
        
        UserProgress progress = userProgressRepository.findByUserId(userId)
                .orElse(new UserProgress(userId));

        int totalSolved = progress.getSolvedQuestions().size();
        
        return new UserMeStatsDTO(totalSolved, progress.getSolvedQuestions());
    }

    public UserProgress getUserProgress(String userId) {
        return userProgressRepository.findByUserId(userId)
                .orElse(new UserProgress(userId));
    }

    public UserProgress createUserProgress(String userId) {
        UserProgress progress = new UserProgress(userId);
        return userProgressRepository.save(progress);
    }

    public UserProgress getOrCreateUserProgress(String userId) {
        return userProgressRepository.findByUserId(userId)
                .orElseGet(() -> createUserProgress(userId));
    }

    /**
     * ✅ NEW: Get question solve status with timestamp
     * Rate limiting handled by RateLimitInterceptor (60/min for reads)
     */
    public QuestionSolveStatusDTO getQuestionSolveStatus(String userId, String questionId) {
        validateQuestionId(questionId);
        
        UserProgress progress = userProgressRepository.findByUserId(userId).orElse(null);
        
        if (progress == null || !progress.isQuestionSolved(questionId)) {
            return QuestionSolveStatusDTO.notSolved();
        }
        
        LocalDateTime solvedAt = progress.getSolvedAt(questionId);
        return QuestionSolveStatusDTO.solved(solvedAt);
    }

    /** 
     * Check if question is solved (returns boolean only)
     * Rate limiting handled by RateLimitInterceptor (60/min for reads)
     */
    public boolean isQuestionSolved(String userId, String questionId) {
        validateQuestionId(questionId);
        
        return userProgressRepository.findByUserId(userId)
                .map(progress -> progress.isQuestionSolved(questionId))
                .orElse(false);
    }

    /**
     * Mark question as solved - evicts ONLY this user's cache
     * Rate limiting handled by RateLimitInterceptor (10/min for writes)
     */
    @CacheEvict(value = "userMeStats", key = "#userId")
    public void markQuestionAsSolved(String userId, String questionId) {
        validateQuestionId(questionId);
        
        int attempt = 0;
        
        while (attempt < MAX_RETRIES) {
            try {
                if (!questionRepository.existsById(questionId)) {
                    throw new QuestionNotFoundException(questionId);
                }

                UserProgress progress = getOrCreateUserProgress(userId);

                if (progress.isQuestionSolved(questionId)) {
                    throw new QuestionAlreadySolvedException(questionId);
                }

                progress.addSolvedQuestion(questionId);
                userProgressRepository.save(progress);
                
                logger.info("✅ User {} marked question {} as solved", userId, questionId);
                return;
                
            } catch (IllegalArgumentException | QuestionNotFoundException | QuestionAlreadySolvedException e) {
                throw e;
                
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    logger.error("❌ Failed to mark question after {} attempts", MAX_RETRIES);
                    throw new ConcurrentModificationException();
                }
                
                logger.warn("⚠️ Optimistic lock conflict, retrying... (attempt {}/{})", 
                        attempt, MAX_RETRIES);
                
                try {
                    Thread.sleep(50 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operation interrupted");
                }
            }
        }
    }

    /**
     * Unmark question - evicts ONLY this user's cache
     * Rate limiting handled by RateLimitInterceptor (10/min for writes)
     */
    @CacheEvict(value = "userMeStats", key = "#userId")
    public void unmarkQuestionAsSolved(String userId, String questionId) {
        validateQuestionId(questionId);
        
        int attempt = 0;
        
        while (attempt < MAX_RETRIES) {
            try {
                UserProgress progress = userProgressRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("User progress not found"));

                if (!progress.isQuestionSolved(questionId)) {
                    throw new QuestionNotSolvedException(questionId);
                }

                progress.removeSolvedQuestion(questionId);
                userProgressRepository.save(progress);
                
                logger.info("✅ User {} unmarked question {}", userId, questionId);
                return;
                
            } catch (IllegalArgumentException | QuestionNotSolvedException e) {
                throw e;
                
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new ConcurrentModificationException();
                }
                
                logger.warn("⚠️ Optimistic lock conflict, retrying...");
                
                try {
                    Thread.sleep(50 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operation interrupted");
                }
            }
        }
    }

    /** for question deletion
     * Remove question from all users - clears ALL caches
     * (Admin operation - no rate limiting needed)
     */
    @CacheEvict(value = "userMeStats", allEntries = true)
    public int removeQuestionFromAllUsers(String questionId) {
        List<UserProgress> allProgress = userProgressRepository.findAll();
        int removedCount = 0;

        for (UserProgress progress : allProgress) {
            if (progress.isQuestionSolved(questionId)) {
                progress.removeSolvedQuestion(questionId);
                userProgressRepository.save(progress);
                removedCount++;
            }
        }

        logger.info("Removed question {} from {} users", questionId, removedCount);
        return removedCount;
    }

    /**  for category deletion
     * Remove questions from all users - clears ALL caches
     * (Admin operation - no rate limiting needed)
     */
    @CacheEvict(value = "userMeStats", allEntries = true)
    public int removeQuestionsFromAllUsers(List<String> questionIds) {
        List<UserProgress> allProgress = userProgressRepository.findAll();
        int totalRemoved = 0;

        for (UserProgress progress : allProgress) {
            int removedFromUser = 0;
            for (String questionId : questionIds) {
                if (progress.isQuestionSolved(questionId)) {
                    progress.removeSolvedQuestion(questionId);
                    removedFromUser++;
                }
            }

            if (removedFromUser > 0) {
                userProgressRepository.save(progress);
                totalRemoved += removedFromUser;
            }
        }

        logger.info("Removed {} question entries from all users", totalRemoved);
        return totalRemoved;
    }
}