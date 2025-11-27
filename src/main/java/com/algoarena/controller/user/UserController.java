// src/main/java/com/algoarena/controller/user/UserController.java
package com.algoarena.controller.user;

import com.algoarena.dto.user.UserMeStatsDTO;
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
     * Returns all solved questions (no sorting, no pagination)
     * Frontend handles sorting/pagination
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserMeStatsDTO> getUserMeStats(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserMeStatsDTO stats = userProgressService.getUserMeStats(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/me/progress/{questionId}")
    public ResponseEntity<Map<String, Boolean>> getQuestionProgress(
            @PathVariable String questionId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        boolean solved = userProgressService.isQuestionSolved(currentUser.getId(), questionId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("solved", solved);
        
        return ResponseEntity.ok(response);
    }   

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