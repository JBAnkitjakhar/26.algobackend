// src/main/java/com/algoarena/controller/user/UserController.java
package com.algoarena.controller.user;

import com.algoarena.dto.user.GlobalCategoryInfoDTO;
import com.algoarena.dto.user.QuestionsMetadataDTO;
import com.algoarena.dto.user.UserMeStatsDTO;
import com.algoarena.dto.user.UserProgressMapDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.CategoryService;
import com.algoarena.service.dsa.QuestionService;
import com.algoarena.service.user.UserStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * User-specific controller for optimized user endpoints
 * All endpoints require authentication
 */
@RestController
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
public class UserController {

    @Autowired
    private UserStatsService userStatsService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private QuestionService questionService;

    /**
     * Get complete ME page statistics
     * Returns all user stats + recent activity in ONE call
     * 
     * @param page Page number for recent solved questions (default: 0)
     * @param size Page size for recent solved questions (default: 15)
     * @param authentication Current user
     * @return UserMeStatsDTO with complete statistics
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserMeStatsDTO> getUserMeStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        UserMeStatsDTO stats = userStatsService.getUserMeStats(currentUser.getId(), page, size);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get global categories information
     * Contains all categories with questions grouped by level (displayOrder)
     * This data is SAME for all users, heavily cached
     * 
     * @return GlobalCategoryInfoDTO with all categories
     */
    @GetMapping("/categories/info")
    public ResponseEntity<GlobalCategoryInfoDTO> getGlobalCategoriesInfo() {
        GlobalCategoryInfoDTO categoriesInfo = categoryService.getGlobalCategoriesInfo();
        return ResponseEntity.ok(categoriesInfo);
    }

    /**
     * Get user's progress map
     * Contains only questions user has solved + approach counts
     * User-specific, cached per user
     * 
     * @param authentication Current user
     * @return UserProgressMapDTO with solved questions
     */
    @GetMapping("/progress/map")
    public ResponseEntity<UserProgressMapDTO> getUserProgressMap(
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        UserProgressMapDTO progressMap = userStatsService.getUserProgressMap(currentUser.getId());
        
        return ResponseEntity.ok(progressMap);
    }

    /**
     * Get questions metadata (lightweight)
     * Contains question ID, title, and level for all questions
     * Used for dropdowns and displaying titles
     * 
     * @return QuestionsMetadataDTO with all questions metadata
     */
    @GetMapping("/questions/metadata")
    public ResponseEntity<QuestionsMetadataDTO> getQuestionsMetadata() {
        QuestionsMetadataDTO metadata = questionService.getQuestionsMetadata();
        return ResponseEntity.ok(metadata);
    }
}