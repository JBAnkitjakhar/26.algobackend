// CREATE NEW FILE: src/main/java/com/algoarena/service/user/UserStatsService.java

package com.algoarena.service.user;

import com.algoarena.dto.user.UserMeStatsDTO;
import com.algoarena.dto.user.UserProgressMapDTO;
import com.algoarena.model.QuestionLevel;
import com.algoarena.model.UserProgress;
import com.algoarena.repository.ApproachRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class UserStatsService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ApproachRepository approachRepository;

    /**
     * Get complete ME page statistics (cached per user)
     * Returns all stats + recent activity in ONE call
     */
    @Cacheable(value = "userMeStats", key = "#userId + '_page_' + #page + '_size_' + #size")
    public UserMeStatsDTO getUserMeStats(String userId, int page, int size) {
        System.out.println("CACHE MISS: Fetching user ME stats for user: " + userId);
        
        UserMeStatsDTO dto = new UserMeStatsDTO();
        
        // Get total questions
        long totalQuestions = questionRepository.count();
        dto.setTotalQuestions((int) totalQuestions);
        
        // Get user's solved questions
        List<UserProgress> userProgress = userProgressRepository.findByUser_IdAndSolved(userId, true);
        dto.setTotalSolved(userProgress.size());
        
        // Calculate progress percentage
        dto.setProgressPercentage(totalQuestions > 0 ? 
            (userProgress.size() * 100.0 / totalQuestions) : 0.0);
        
        // Calculate level-wise stats
        UserMeStatsDTO.LevelStatsDTO levelStats = new UserMeStatsDTO.LevelStatsDTO();
        levelStats.setEasyTotal((int) questionRepository.countByLevel(QuestionLevel.EASY));
        levelStats.setMediumTotal((int) questionRepository.countByLevel(QuestionLevel.MEDIUM));
        levelStats.setHardTotal((int) questionRepository.countByLevel(QuestionLevel.HARD));
        
        // Count solved by level
        long easySolved = userProgress.stream()
            .filter(p -> p.getLevel() == QuestionLevel.EASY)
            .count();
        long mediumSolved = userProgress.stream()
            .filter(p -> p.getLevel() == QuestionLevel.MEDIUM)
            .count();
        long hardSolved = userProgress.stream()
            .filter(p -> p.getLevel() == QuestionLevel.HARD)
            .count();
        
        levelStats.setEasySolved((int) easySolved);
        levelStats.setMediumSolved((int) mediumSolved);
        levelStats.setHardSolved((int) hardSolved);
        dto.setProgressByLevel(levelStats);
        
        // Calculate recent solved (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long recentCount = userProgress.stream()
            .filter(p -> p.getSolvedAt() != null && p.getSolvedAt().isAfter(sevenDaysAgo))
            .count();
        dto.setRecentSolvedCount((int) recentCount);
        
        // Get recent solved questions with pagination (newest first)
        List<UserProgress> sortedProgress = userProgress.stream()
            .sorted(Comparator.comparing(UserProgress::getSolvedAt).reversed())
            .toList();
        
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, sortedProgress.size());
        
        List<UserMeStatsDTO.RecentSolvedQuestionDTO> recentSolved = new ArrayList<>();
        
        if (fromIndex < sortedProgress.size()) {
            List<UserProgress> pageProgress = sortedProgress.subList(fromIndex, toIndex);
            
            for (UserProgress progress : pageProgress) {
                if (progress.getQuestion() != null) {
                    UserMeStatsDTO.RecentSolvedQuestionDTO recentDto = 
                        new UserMeStatsDTO.RecentSolvedQuestionDTO();
                    
                    recentDto.setQuestionId(progress.getQuestion().getId());
                    recentDto.setTitle(progress.getQuestion().getTitle());
                    recentDto.setLevel(progress.getLevel());
                    recentDto.setSolvedAt(progress.getSolvedAt());
                    
                    // Get category name
                    if (progress.getQuestion().getCategory() != null) {
                        recentDto.setCategory(progress.getQuestion().getCategory().getName());
                    }
                    
                    // Get approach count for this question by this user
                    long approachCount = approachRepository.countByQuestion_IdAndUser_Id(
                        progress.getQuestion().getId(), userId);
                    recentDto.setApproachCount((int) approachCount);
                    
                    recentSolved.add(recentDto);
                }
            }
        }
        
        dto.setRecentSolvedQuestions(recentSolved);
        
        return dto;
    }

    /**
     * Get user's progress map (cached per user)
     * Returns only solved questions with approach counts
     */
    @Cacheable(value = "userProgressMap", key = "#userId")
    public UserProgressMapDTO getUserProgressMap(String userId) {
        System.out.println("CACHE MISS: Fetching user progress map for user: " + userId);
        
        List<UserProgress> userProgress = userProgressRepository.findByUser_IdAndSolved(userId, true);
        
        Map<String, UserProgressMapDTO.QuestionProgress> progressMap = new HashMap<>();
        
        for (UserProgress progress : userProgress) {
            if (progress.getQuestion() != null) {
                String questionId = progress.getQuestion().getId();
                
                // Get approach count for this question
                long approachCount = approachRepository.countByQuestion_IdAndUser_Id(questionId, userId);
                
                UserProgressMapDTO.QuestionProgress questionProgress = 
                    new UserProgressMapDTO.QuestionProgress(
                        progress.getSolvedAt(),
                        (int) approachCount
                    );
                
                progressMap.put(questionId, questionProgress);
            }
        }
        
        UserProgressMapDTO result = new UserProgressMapDTO();
        result.setSolvedQuestions(progressMap);
        
        return result;
    }

    /**
     * Evict user stats cache when user solves a question
     */
    @CacheEvict(value = { "userMeStats", "userProgressMap" }, key = "#userId", allEntries = false)
    public void evictUserStatsCache(String userId) {
        System.out.println("Evicting user stats cache for user: " + userId);
    }
}