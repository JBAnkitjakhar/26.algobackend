// src/main/java/com/algoarena/controller/course/CourseController.java
package com.algoarena.controller.course;

import com.algoarena.dto.course.CourseDocDTO;
import com.algoarena.dto.course.CourseTopicDTO;
import com.algoarena.model.User;
import com.algoarena.service.course.CourseDocService;
import com.algoarena.service.course.CourseTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseTopicService topicService;

    @Autowired
    private CourseDocService docService;

    // ==================== PUBLIC ENDPOINTS (User can read) ====================

    /**
     * API 1: Get all topics with document count
     * GET /api/courses/topics
     * 
     * Returns: List of topics with how many docs in each topic
     * Used in: Course topics listing page
     */
    @GetMapping("/topics")
    public ResponseEntity<Map<String, Object>> getAllTopics() {
        try {
            List<CourseTopicDTO> topics = topicService.getAllTopicsWithDocCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", topics);
            response.put("count", topics.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch topics");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * API 2: Get all documents for a specific topic (WITHOUT full content)
     * GET /api/courses/topics/{topicId}/docs
     * 
     * Returns: List of documents with title and metadata ONLY (no content blocks)
     * Used in: Topic's document listing page
     */
    @GetMapping("/topics/{topicId}/docs")
    public ResponseEntity<Map<String, Object>> getDocsByTopic(@PathVariable String topicId) {
        try {
            // Get topic info
            CourseTopicDTO topic = topicService.getTopicById(topicId);
            
            // Get docs WITHOUT content (optimized for listing)
            List<CourseDocDTO> docs = docService.getDocsByTopic(topicId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("docs", docs);
            response.put("count", docs.size());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch documents");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch documents");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * API 3: Get single document WITH full content
     * GET /api/courses/docs/{docId}
     * 
     * Returns: Complete document with all content blocks (text, images, code)
     * Used in: Document reading page
     */
    @GetMapping("/docs/{docId}")
    public ResponseEntity<Map<String, Object>> getDocById(@PathVariable String docId) {
        try {
            CourseDocDTO doc = docService.getDocById(docId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", doc);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Document not found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch document");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get single topic by ID
     * GET /api/courses/topics/{topicId}
     */
    @GetMapping("/topics/{topicId}")
    public ResponseEntity<Map<String, Object>> getTopicById(@PathVariable String topicId) {
        try {
            CourseTopicDTO topic = topicService.getTopicById(topicId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", topic);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Topic not found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch topic");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Create new topic (Admin only)
     * POST /api/courses/topics
     */
    @PostMapping("/topics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> createTopic(
            @Valid @RequestBody CourseTopicDTO topicDTO,
            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            CourseTopicDTO createdTopic = topicService.createTopic(topicDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdTopic);
            response.put("message", "Topic created successfully");
            
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create topic");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create topic");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Update topic (Admin only)
     * PUT /api/courses/topics/{topicId}
     */
    @PutMapping("/topics/{topicId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> updateTopic(
            @PathVariable String topicId,
            @Valid @RequestBody CourseTopicDTO topicDTO,
            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            CourseTopicDTO updatedTopic = topicService.updateTopic(topicId, topicDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedTopic);
            response.put("message", "Topic updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update topic");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update topic");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Delete topic (Admin only)
     * DELETE /api/courses/topics/{topicId}
     */
    @DeleteMapping("/topics/{topicId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTopic(@PathVariable String topicId) {
        try {
            topicService.deleteTopic(topicId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Topic deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete topic");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete topic");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Create new document (Admin only)
     * POST /api/courses/docs
     */
    @PostMapping("/docs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> createDoc(
            @Valid @RequestBody CourseDocDTO docDTO,
            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            CourseDocDTO createdDoc = docService.createDoc(docDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdDoc);
            response.put("message", "Document created successfully");
            
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create document");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create document");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Update document (Admin only)
     * PUT /api/courses/docs/{docId}
     */
    @PutMapping("/docs/{docId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> updateDoc(
            @PathVariable String docId,
            @Valid @RequestBody CourseDocDTO docDTO,
            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            CourseDocDTO updatedDoc = docService.updateDoc(docId, docDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedDoc);
            response.put("message", "Document updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update document");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update document");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Delete document (Admin only)
     * DELETE /api/courses/docs/{docId}
     */
    @DeleteMapping("/docs/{docId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteDoc(@PathVariable String docId) {
        try {
            docService.deleteDoc(docId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete document");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete document");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ==================== STATISTICS ENDPOINTS ====================

    /**
     * Get course statistics
     * GET /api/courses/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCourseStats() {
        try {
            CourseTopicService.TopicStatsDTO stats = topicService.getTopicStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch statistics");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}