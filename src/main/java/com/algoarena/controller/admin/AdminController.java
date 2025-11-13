// src/main/java/com/algoarena/controller/admin/AdminController.java
package com.algoarena.controller.admin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.algoarena.dto.dsa.AdminQuestionSummaryDTO;
import com.algoarena.dto.dsa.AdminSolutionSummaryDTO;
import com.algoarena.service.dsa.QuestionService;
import com.algoarena.service.dsa.SolutionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-specific controller for summary/lightweight endpoints
 * These endpoints return minimal data for listing views
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
public class AdminController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SolutionService solutionService;

    /**
     * Get admin questions summary (lightweight, paginated)
     * Returns minimal data without full content
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Page of AdminQuestionSummaryDTO
     */
    @GetMapping("/questions/summary")
    public ResponseEntity<Page<AdminQuestionSummaryDTO>> getAdminQuestionsSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminQuestionSummaryDTO> summaries = questionService.getAdminQuestionsSummary(pageable);
        
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get admin solutions summary (lightweight, paginated)
     * Returns minimal data without full content
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Page of AdminSolutionSummaryDTO
     */
    @GetMapping("/solutions/summary")
    public ResponseEntity<Page<AdminSolutionSummaryDTO>> getAdminSolutionsSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminSolutionSummaryDTO> summaries = solutionService.getAdminSolutionsSummary(pageable);
        
        return ResponseEntity.ok(summaries);
    }

    /**
     * Update display order for a single question
     * 
     * @param id Question ID
     * @param displayOrder New display order value
     * @return Success response
     */
    @PutMapping("/questions/{id}/display-order")
    public ResponseEntity<Map<String, Object>> updateQuestionDisplayOrder(
            @PathVariable String id,
            @RequestParam Integer displayOrder) {
        
        try {
            questionService.updateQuestionDisplayOrder(id, displayOrder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Display order updated successfully");
            response.put("questionId", id);
            response.put("displayOrder", displayOrder);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Batch update display order for multiple questions
     * Used for drag-and-drop reordering in admin panel
     * 
     * Request body format:
     * [
     *   {"questionId": "id1", "displayOrder": 1},
     *   {"questionId": "id2", "displayOrder": 2},
     *   ...
     * ]
     */
    @PutMapping("/questions/display-order/batch")
    public ResponseEntity<Map<String, Object>> batchUpdateDisplayOrder(
            @RequestBody List<Map<String, Object>> updates) {
        
        try {
            int updatedCount = questionService.batchUpdateDisplayOrder(updates);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Display orders updated successfully");
            response.put("updatedCount", updatedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get questions by category and level with display order
     * Used for admin reordering interface
     * 
     * @param categoryId Category ID
     * @param level Question level (EASY, MEDIUM, HARD)
     * @return List of questions with display order
     */
    @GetMapping("/questions/by-category-level")
    public ResponseEntity<List<Map<String, Object>>> getQuestionsByCategoryAndLevel(
            @RequestParam String categoryId,
            @RequestParam String level) {
        
        try {
            List<Map<String, Object>> questions = 
                questionService.getQuestionsByCategoryAndLevelForOrdering(categoryId, level);
            
            return ResponseEntity.ok(questions);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Reset display order for a category and level
     * Re-orders questions based on current order (1, 2, 3, ...)
     * 
     * @param categoryId Category ID
     * @param level Question level
     * @return Success response
     */
    @PostMapping("/questions/display-order/reset")
    public ResponseEntity<Map<String, Object>> resetDisplayOrder(
            @RequestParam String categoryId,
            @RequestParam String level) {
        
        try {
            int updatedCount = questionService.resetDisplayOrder(categoryId, level);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Display orders reset successfully");
            response.put("updatedCount", updatedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}