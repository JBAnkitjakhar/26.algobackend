// src/main/java/com/algoarena/service/admin/AdminOverviewService.java
package com.algoarena.service.admin;

import com.algoarena.dto.admin.AdminOverviewDTO;
import com.algoarena.dto.admin.LoggedInUserDTO;
import com.algoarena.model.User;
import com.algoarena.model.UserApproaches;
import com.algoarena.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.algoarena.repository.UserApproachesRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private UserApproachesRepository userApproachesRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Generate complete admin overview statistics
     * This method aggregates all stats needed for the admin overview page
     * 
     * @return AdminOverviewDTO with all statistics
     */
    public AdminOverviewDTO getAdminOverview() {
        // ✅ FIXED: Calculate today's 24-hour range for IST (UTC + 5:30)
        // Database stores UTC, so we subtract 5 hours 30 minutes to get IST midnight
        LocalDateTime todayStart = LocalDate.now().atStartOfDay().minusHours(5).minusMinutes(30); // 12:00 AM IST = 6:30 PM UTC (previous day)
        LocalDateTime todayEnd = todayStart.plusDays(1); // Next day 6:30 PM UTC = 12:00 AM IST tomorrow
        
        
        // Get users who logged in today (between 12 AM IST today and 12 AM IST tomorrow)
        List<LoggedInUserDTO> loggedInUsers = getUsersLoggedInTodayDetails(todayStart, todayEnd);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Build overview DTO using builder pattern
        AdminOverviewDTO overview = new AdminOverviewDTO.Builder()
                .totalUsers(getUserCount())
                .totalCategories(getCategoryCount())
                .totalQuestions(getQuestionCount())
                .totalSolutions(getSolutionCount())
                .totalUserApproaches(getUserApproachCount())
                .usersLoggedInToday(loggedInUsers.size())
                .usersLoggedInTodayDetails(loggedInUsers)
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
        // Count total approaches across all users
        return userApproachesRepository.findAll().stream()
                .mapToLong(UserApproaches::getTotalApproaches)
                .sum();
    }

    /**
     * ✅ FIXED: Get details of users who logged in today (12 AM IST to 12 AM IST)
     * Queries users with lastLogin between todayStart and todayEnd (in UTC)
     */
    private List<LoggedInUserDTO> getUsersLoggedInTodayDetails(LocalDateTime todayStart, LocalDateTime todayEnd) {
        // Query for users whose lastLogin is >= todayStart AND < todayEnd
        Query query = new Query(Criteria.where("lastLogin")
                .gte(todayStart)
                .lt(todayEnd));

        List<User> users = mongoTemplate.find(query, User.class);

        return users.stream()
                .map(user -> new LoggedInUserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getGithubUsername(),
                        user.getImage(),
                        user.getLastLogin())) // Pass lastLogin timestamp
                .collect(Collectors.toList());
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
     * Get count of new users in the last N days
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
            // Check database connectivity by attempting a simple query
            mongoTemplate.count(new Query(), "users");
            health.setDatabaseConnected(true);
            health.setDatabaseStatus("Connected - MongoDB is operational");
        } catch (Exception e) {
            health.setDatabaseConnected(false);
            health.setDatabaseStatus("Error: " + e.getMessage());
        }

        return health;
    }
}