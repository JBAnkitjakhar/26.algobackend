// src/main/java/com/algoarena/service/user/UserProgressService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.user.UserMeStatsDTO;
import com.algoarena.dto.user.UserMeStatsDTO.SolvedQuestionInfo;
import com.algoarena.dto.user.UserMeStatsDTO.StatsOverview;
import com.algoarena.dto.user.UserMeStatsDTO.PaginatedSolvedQuestions;
import com.algoarena.model.Question;
import com.algoarena.model.UserProgress;
import com.algoarena.model.UserProgress.SolvedQuestion;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.UserProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProgressService {

    private static final Logger logger = LoggerFactory.getLogger(UserProgressService.class);

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Cacheable(value = "userMeStats", key = "#userId + '-' + #page + '-' + #size")
    public UserMeStatsDTO getUserMeStats(String userId, int page, int size) {
        UserProgress progress = userProgressRepository.findByUserId(userId)
                .orElse(new UserProgress(userId));
        
        StatsOverview stats = new StatsOverview(
            progress.getTotalSolved(),
            progress.getEasySolved(),
            progress.getMediumSolved(),
            progress.getHardSolved(),
            progress.getLastSolvedAt()
        );
        
        List<SolvedQuestionInfo> allSolved = progress.getSolvedQuestions().values().stream()
                .sorted(Comparator.comparing(SolvedQuestion::getSolvedAt).reversed())
                .map(sq -> new SolvedQuestionInfo(
                    sq.getQuestionId(),
                    sq.getTitle(),
                    sq.getCategory(),
                    sq.getLevel(),
                    sq.getSolvedAt()
                ))
                .collect(Collectors.toList());
        
        long totalElements = allSolved.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, allSolved.size());
        
        List<SolvedQuestionInfo> pagedQuestions = new ArrayList<>();
        if (start < totalElements) {
            pagedQuestions = allSolved.subList(start, end);
        }
        
        PaginatedSolvedQuestions paginatedQuestions = new PaginatedSolvedQuestions(
            pagedQuestions,
            page,
            size,
            totalElements,
            totalPages,
            page < totalPages - 1,
            page > 0
        );
        
        return new UserMeStatsDTO(stats, paginatedQuestions);
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

    @CacheEvict(value = "userMeStats", allEntries = true)
    public void markQuestionAsSolved(String userId, String questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        UserProgress progress = getOrCreateUserProgress(userId);
        
        if (progress.isQuestionSolved(questionId)) {
            throw new RuntimeException("Question already marked as solved");
        }
        
        progress.addSolvedQuestion(
            questionId,
            question.getTitle(),
            question.getCategory().getName(),
            question.getLevel()
        );
        
        userProgressRepository.save(progress);
    }

    public boolean isQuestionSolved(String userId, String questionId) {
        return userProgressRepository.findByUserId(userId)
                .map(progress -> progress.isQuestionSolved(questionId))
                .orElse(false);
    }

    @CacheEvict(value = "userMeStats", allEntries = true)
    public void unmarkQuestionAsSolved(String userId, String questionId) {
        UserProgress progress = userProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User progress not found"));
        
        if (!progress.isQuestionSolved(questionId)) {
            throw new RuntimeException("Question not solved by user");
        }
        
        progress.removeSolvedQuestion(questionId);
        userProgressRepository.save(progress);
    }

    @CacheEvict(value = "userMeStats", allEntries = true)
    public int removeQuestionFromAllUsers(String questionId) {
        List<UserProgress> allProgress = userProgressRepository.findAll();
        int removedCount = 0;
        
        for (UserProgress progress : allProgress) {
            if (progress.isQuestionSolved(questionId)) {
                progress.removeSolvedQuestion(questionId);
                userProgressRepository.save(progress);
                removedCount++;
                logger.info("Removed question {} from user {}", questionId, progress.getUserId());
            }
        }
        
        logger.info("Removed question {} from {} users' progress", questionId, removedCount);
        return removedCount;
    }

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
                logger.info("Removed {} questions from user {}", removedFromUser, progress.getUserId());
            }
        }
        
        logger.info("Removed total {} question entries from all users' progress", totalRemoved);
        return totalRemoved;
    }

    
}