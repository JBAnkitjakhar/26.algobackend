// src/main/java/com/algoarena/controller/dsa/ApproachController.java
package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.ApproachDetailDTO;
import com.algoarena.dto.dsa.ApproachMetadataDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.ApproachService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/approaches")
@PreAuthorize("isAuthenticated()")
public class ApproachController {

    @Autowired
    private ApproachService approachService;

    /**
     * GET /api/approaches/question/{questionId}
     * Get current user's approaches metadata for a question
     * Returns: List of metadata (no full content)
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<ApproachMetadataDTO>> getMyApproachesForQuestion(
            @PathVariable String questionId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        List<ApproachMetadataDTO> approaches = approachService.getMyApproachesForQuestion(
            currentUser.getId(), 
            questionId
        );
        return ResponseEntity.ok(approaches);
    }

    /**
     * GET /api/approaches/question/{questionId}/{approachId}
     * Get full content for a specific approach
     */
    @GetMapping("/question/{questionId}/{approachId}")
    public ResponseEntity<ApproachDetailDTO> getMyApproachDetail(
            @PathVariable String questionId,
            @PathVariable String approachId,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            ApproachDetailDTO approach = approachService.getMyApproachDetail(
                currentUser.getId(), 
                questionId, 
                approachId
            );
            return ResponseEntity.ok(approach);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/approaches/question/{questionId}
     * Create new approach for current user
     */
    @PostMapping("/question/{questionId}")
    public ResponseEntity<Map<String, Object>> createApproach(
            @PathVariable String questionId,
            @Valid @RequestBody ApproachDetailDTO dto,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            ApproachDetailDTO created = approachService.createApproach(
                currentUser.getId(), 
                questionId, 
                dto, 
                currentUser
            );
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Approach created successfully",
                "data", created
            );
            
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PUT /api/approaches/question/{questionId}/{approachId}
     * Update current user's approach
     */
    @PutMapping("/question/{questionId}/{approachId}")
    public ResponseEntity<Map<String, Object>> updateApproach(
            @PathVariable String questionId,
            @PathVariable String approachId,
            @Valid @RequestBody ApproachDetailDTO dto,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            ApproachDetailDTO updated = approachService.updateApproach(
                currentUser.getId(), 
                questionId, 
                approachId, 
                dto
            );
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Approach updated successfully",
                "data", updated
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * DELETE /api/approaches/question/{questionId}/{approachId}
     * Delete current user's approach
     */
    @DeleteMapping("/question/{questionId}/{approachId}")
    public ResponseEntity<Map<String, Object>> deleteApproach(
            @PathVariable String questionId,
            @PathVariable String approachId,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            approachService.deleteApproach(currentUser.getId(), questionId, approachId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Approach deleted successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /api/approaches/my-approaches
     * Get all approaches by current user
     */
    @GetMapping("/my-approaches")
    public ResponseEntity<List<ApproachMetadataDTO>> getMyAllApproaches(
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        List<ApproachMetadataDTO> approaches = approachService.getMyAllApproaches(currentUser.getId());
        return ResponseEntity.ok(approaches);
    }

    /**
     * GET /api/approaches/question/{questionId}/usage
     * Get usage stats for current user on a question
     * Shows remaining space and slots
     */
    @GetMapping("/question/{questionId}/usage")
    public ResponseEntity<Map<String, Object>> getMyQuestionUsage(
            @PathVariable String questionId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> usage = approachService.getMyQuestionUsage(
            currentUser.getId(), 
            questionId
        );
        return ResponseEntity.ok(usage);
    }
}