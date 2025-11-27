// src/main/java/com/algoarena/controller/dsa/QuestionController.java
package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.QuestionDTO;
import com.algoarena.dto.user.QuestionsMetadataDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // ============================================
    // ADMIN ENDPOINTS
    // ============================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<QuestionDTO> createQuestion(
            @Valid @RequestBody QuestionDTO questionDTO,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        QuestionDTO createdQuestion = questionService.createQuestion(questionDTO, currentUser);
        return ResponseEntity.status(201).body(createdQuestion);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody QuestionDTO questionDTO) {
        try {
            QuestionDTO updatedQuestion = questionService.updateQuestion(id, questionDTO);
            return ResponseEntity.ok(updatedQuestion);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, String>> deleteQuestion(@PathVariable String id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok(Map.of("success", "true"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================
    // USER ENDPOINTS (Rate Limited: 30/min)
    // ============================================

    /**
     * Get complete question details for authenticated users
     * Rate limited: 30 requests per minute per user
     * Globally cached for all users
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable String id) {
        try {
            QuestionDTO question = questionService.getQuestionById(id);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get questions metadata (lightweight)
     * Rate limited: 30 requests per minute per user
     * Contains question ID, title, level, and categoryId for all questions
     */
    @GetMapping("/metadata")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionsMetadataDTO> getQuestionsMetadata() {
        QuestionsMetadataDTO metadata = questionService.getQuestionsMetadata();
        return ResponseEntity.ok(metadata);
    }
}