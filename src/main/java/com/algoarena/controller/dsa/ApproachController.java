// src/main/java/com/algoarena/controller/dsa/ApproachController.java
package com.algoarena.controller.dsa;

import com.algoarena.config.RateLimitConfig;
import com.algoarena.dto.dsa.ApproachDetailDTO;
import com.algoarena.dto.dsa.ApproachMetadataDTO;
import com.algoarena.exception.RateLimitExceededException;
import com.algoarena.model.User;
import com.algoarena.service.dsa.ApproachService;
import io.github.bucket4j.Bucket;
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

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<ApproachMetadataDTO>> getMyApproachesForQuestion(
            @PathVariable String questionId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        Bucket bucket = rateLimitConfig.resolveApproachReadBucket(currentUser.getId());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }

        List<ApproachMetadataDTO> approaches = approachService.getMyApproachesForQuestion(
            currentUser.getId(), 
            questionId
        );
        return ResponseEntity.ok(approaches);
    }

    @GetMapping("/question/{questionId}/{approachId}")
    public ResponseEntity<ApproachDetailDTO> getMyApproachDetail(
            @PathVariable String questionId,
            @PathVariable String approachId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        Bucket bucket = rateLimitConfig.resolveApproachReadBucket(currentUser.getId());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }

        try {
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

    @PostMapping("/question/{questionId}")
    public ResponseEntity<Map<String, Object>> createApproach(
            @PathVariable String questionId,
            @Valid @RequestBody ApproachDetailDTO dto,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        Bucket bucket = rateLimitConfig.resolveApproachWriteBucket(currentUser.getId());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }

        try {
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

    @PutMapping("/question/{questionId}/{approachId}")
    public ResponseEntity<Map<String, Object>> updateApproach(
            @PathVariable String questionId,
            @PathVariable String approachId,
            @Valid @RequestBody ApproachDetailDTO dto,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        Bucket bucket = rateLimitConfig.resolveApproachWriteBucket(currentUser.getId());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }

        try {
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

    @DeleteMapping("/question/{questionId}/{approachId}")
    public ResponseEntity<Map<String, Object>> deleteApproach(
            @PathVariable String questionId,
            @PathVariable String approachId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        Bucket bucket = rateLimitConfig.resolveApproachWriteBucket(currentUser.getId());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }

        try {
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

    @GetMapping("/question/{questionId}/usage")
    public ResponseEntity<Map<String, Object>> getMyQuestionUsage(
            @PathVariable String questionId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        Bucket bucket = rateLimitConfig.resolveApproachReadBucket(currentUser.getId());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }

        Map<String, Object> usage = approachService.getMyQuestionUsage(
            currentUser.getId(), 
            questionId
        );
        return ResponseEntity.ok(usage);
    }
}