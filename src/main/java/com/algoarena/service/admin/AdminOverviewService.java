// src/main/java/com/algoarena/service/admin/AdminOverviewService.java
package com.algoarena.service.admin;

import com.algoarena.dto.admin.AdminOverviewDTO;
import com.algoarena.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service for generating admin overview statistics
 */
@Service
public class AdminOverviewService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private ApproachRepository approachRepository;

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Value("${spring.application.version:1.0.0}")
    private String appVersion;

    /**
     * Generate complete admin overview statistics
     * This method aggregates all stats needed for the admin overview page
     * 
     * @return AdminOverviewDTO with all statistics
     */
    public AdminOverviewDTO getAdminOverview() {
        // Calculate date ranges
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        // Build overview DTO using builder pattern
        AdminOverviewDTO overview = new AdminOverviewDTO.Builder()
                .totalUsers(getUserCount())
                .totalCategories(getCategoryCount())
                .totalQuestions(getQuestionCount())
                .totalSolutions(getSolutionCount())
                .totalUserApproaches(getUserApproachCount())
                .usersLoggedInToday(getUsersLoggedInToday(todayStart, todayEnd))
                .questionsLast7Days(getQuestionsCreatedSince(sevenDaysAgo))
                .solutionsLast7Days(getSolutionsCreatedSince(sevenDaysAgo))
                .newUsersLast7Days(getNewUsersSince(sevenDaysAgo))
                .systemHealth(checkSystemHealth())
                .build();
        
        return overview;
    }
    
    /**
     * Get total user count
     */
    private long getUserCount() {
        return userRepository.count();
    }
    
    /**
     * Get total category count
     */
    private long getCategoryCount() {
        return categoryRepository.count();
    }
    
    /**
     * Get total question count
     */
    private long getQuestionCount() {
        return questionRepository.count();
    }
    
    /**
     * Get total solution count
     */
    private long getSolutionCount() {
        return solutionRepository.count();
    }
    
    /**
     * Get total user approach count
     */
    private long getUserApproachCount() {
        return approachRepository.count();
    }
    
    /**
     * Get count of users who logged in today
     */
    private long getUsersLoggedInToday(LocalDateTime todayStart, LocalDateTime todayEnd) {
        Query query = new Query(Criteria.where("lastLogin")
                .gte(todayStart)
                .lte(todayEnd));
        return mongoTemplate.count(query, "users");
    }
    
    /**
     * Get count of questions created in the last N days
     */
    private long getQuestionsCreatedSince(LocalDateTime since) {
        Query query = new Query(Criteria.where("createdAt").gte(since));
        return mongoTemplate.count(query, "questions");
    }
    
    /**
     * Get count of solutions created in the last N days
     */
    private long getSolutionsCreatedSince(LocalDateTime since) {
        Query query = new Query(Criteria.where("createdAt").gte(since));
        return mongoTemplate.count(query, "solutions");
    }
    
    /**
     * Get count of new users joined in the last N days
     */
    private long getNewUsersSince(LocalDateTime since) {
        Query query = new Query(Criteria.where("createdAt").gte(since));
        return mongoTemplate.count(query, "users");
    }
    
    /**
     * Check system health status
     */
    private AdminOverviewDTO.SystemHealthStatus checkSystemHealth() {
        AdminOverviewDTO.SystemHealthStatus health = new AdminOverviewDTO.SystemHealthStatus();
        
        try {
            // Check MongoDB connection
            mongoTemplate.getDb().listCollectionNames().first();
            health.setDatabaseConnected(true);
            health.setDatabaseStatus("Connected - MongoDB is operational");
        } catch (Exception e) {
            health.setDatabaseConnected(false);
            health.setDatabaseStatus("Error: " + e.getMessage());
        }
        
        health.setAppVersion(appVersion);
        
        return health;
    }
    
    /**
     * Get statistics by date range (useful for custom reports)
     */
    public AdminOverviewDTO getOverviewByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate today's range for login stats
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        
        // Build overview with custom date range
        AdminOverviewDTO overview = new AdminOverviewDTO.Builder()
                .totalUsers(getUserCount())
                .totalCategories(getCategoryCount())
                .totalQuestions(getQuestionCount())
                .totalSolutions(getSolutionCount())
                .totalUserApproaches(getUserApproachCount())
                .usersLoggedInToday(getUsersLoggedInToday(todayStart, todayEnd))
                .questionsLast7Days(getQuestionsCreatedBetween(startDate, endDate))
                .solutionsLast7Days(getSolutionsCreatedBetween(startDate, endDate))
                .newUsersLast7Days(getNewUsersBetween(startDate, endDate))
                .systemHealth(checkSystemHealth())
                .build();
        
        return overview;
    }
    
    /**
     * Get count of questions created between dates
     */
    private long getQuestionsCreatedBetween(LocalDateTime start, LocalDateTime end) {
        Query query = new Query(Criteria.where("createdAt").gte(start).lte(end));
        return mongoTemplate.count(query, "questions");
    }
    
    /**
     * Get count of solutions created between dates
     */
    private long getSolutionsCreatedBetween(LocalDateTime start, LocalDateTime end) {
        Query query = new Query(Criteria.where("createdAt").gte(start).lte(end));
        return mongoTemplate.count(query, "solutions");
    }
    
    /**
     * Get count of new users between dates
     */
    private long getNewUsersBetween(LocalDateTime start, LocalDateTime end) {
        Query query = new Query(Criteria.where("createdAt").gte(start).lte(end));
        return mongoTemplate.count(query, "users");
    }
}