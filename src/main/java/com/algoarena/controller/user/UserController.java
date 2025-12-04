// src/main/java/com/algoarena/controller/user/UserController.java
package com.algoarena.controller.user;

import com.algoarena.dto.user.UserMeStatsDTO;
import com.algoarena.dto.user.QuestionSolveStatusDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
public class UserController {

    @Autowired
    private UserProgressService userProgressService;

    /**
     * GET /api/user/me/stats
     * Returns all solved questions with timestamps (no sorting, no pagination)
     * Frontend handles sorting/pagination
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserMeStatsDTO> getUserMeStats(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserMeStatsDTO stats = userProgressService.getUserMeStats(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * ✅ NEW: GET /api/user/me/progress/{questionId}
     * Returns detailed solve status with timestamp
     * 
     * Response examples:
     * Not solved: { "solved": false, "solvedAt": null }
     * Solved: { "solved": true, "solvedAt": "2025-12-04T10:30:00" }
     */
    @GetMapping("/me/progress/{questionId}")
    public ResponseEntity<QuestionSolveStatusDTO> getQuestionSolveStatus(
            @PathVariable String questionId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        QuestionSolveStatusDTO status = userProgressService.getQuestionSolveStatus(
            currentUser.getId(), 
            questionId
        );
        
        return ResponseEntity.ok(status);
    }

    /**
     * POST /api/user/me/mark/{questionId}
     * Mark question as solved (adds current timestamp) http://localhost:8080/api/user/me/mark/questionid
     */
    @PostMapping("/me/mark/{questionId}")
    public ResponseEntity<Map<String, Object>> markQuestion(
            @PathVariable String questionId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        userProgressService.markQuestionAsSolved(currentUser.getId(), questionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Question marked as solved");
        
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/user/me/unmark/{questionId}
     * Unmark question (removes from solved list)
     */
    @DeleteMapping("/me/unmark/{questionId}")
    public ResponseEntity<Map<String, Object>> unmarkQuestion(
            @PathVariable String questionId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        userProgressService.unmarkQuestionAsSolved(currentUser.getId(), questionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Question unmarked");
        
        return ResponseEntity.ok(response);
    }
}