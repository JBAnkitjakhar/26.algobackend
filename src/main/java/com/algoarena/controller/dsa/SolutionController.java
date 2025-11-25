// src/main/java/com/algoarena/controller/dsa/SolutionController.java
package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.SolutionDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.SolutionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/solutions")
public class SolutionController {

    @Autowired
    private SolutionService solutionService;

    @GetMapping("/{id}") //  specific solution
    public ResponseEntity<SolutionDTO> getSolutionById(@PathVariable String id) {
        SolutionDTO solution = solutionService.getSolutionById(id);
        if (solution == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(solution);
    }
 
    @GetMapping("/question/{questionId}")  // all solutions belong to that question id
    public ResponseEntity<List<SolutionDTO>> getSolutionsByQuestion(@PathVariable String questionId) {
        List<SolutionDTO> solutions = solutionService.getSolutionsByQuestion(questionId);
        return ResponseEntity.ok(solutions);
    }

    @PostMapping("/question/{questionId}") // create new solution
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> createSolution(
            @PathVariable String questionId,
            @Valid @RequestBody SolutionDTO solutionDTO,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        SolutionDTO createdSolution = solutionService.createSolution(questionId, solutionDTO, currentUser);
        return ResponseEntity.status(201).body(createdSolution);
    }

    @PutMapping("/{id}")// solution id
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> updateSolution(
            @PathVariable String id,
            @Valid @RequestBody SolutionDTO solutionDTO
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.updateSolution(id, solutionDTO);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, String>> deleteSolution(@PathVariable String id) {
        try {
            solutionService.deleteSolution(id);
            return ResponseEntity.ok(Map.of("success", "true"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add image to solution
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> addImageToSolution(
            @PathVariable String id,
            @RequestParam String imageUrl
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.addImageToSolution(id, imageUrl);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove image from solution
     */
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> removeImageFromSolution(
            @PathVariable String id,
            @RequestParam String imageUrl
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.removeImageFromSolution(id, imageUrl);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add visualizer to solution
     */
    @PostMapping("/{id}/visualizers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> addVisualizerToSolution(
            @PathVariable String id,
            @RequestParam String visualizerFileId
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.addVisualizerToSolution(id, visualizerFileId);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove visualizer from solution
     */
    @DeleteMapping("/{id}/visualizers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> removeVisualizerFromSolution(
            @PathVariable String id,
            @RequestParam String visualizerFileId
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.removeVisualizerFromSolution(id, visualizerFileId);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Validate YouTube link and extract video info
     */
    @PostMapping("/validate-youtube")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> validateYoutubeLink(
            @RequestBody Map<String, String> request
    ) {
        String youtubeLink = request.get("youtubeLink");
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (youtubeLink == null || youtubeLink.trim().isEmpty()) {
                response.put("valid", false);
                response.put("error", "YouTube link is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create a temporary DTO to validate
            SolutionDTO tempDTO = new SolutionDTO();
            tempDTO.setYoutubeLink(youtubeLink);
            
            response.put("valid", tempDTO.hasValidYoutubeLink());
            response.put("videoId", tempDTO.getYoutubeVideoId());
            response.put("embedUrl", tempDTO.getYoutubeEmbedUrl());
            response.put("originalUrl", youtubeLink);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Validate Google Drive link
     */
    @PostMapping("/validate-drive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> validateDriveLink(
            @RequestBody Map<String, String> request
    ) {
        String driveLink = request.get("driveLink");
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (driveLink == null || driveLink.trim().isEmpty()) {
                response.put("valid", false);
                response.put("error", "Drive link is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create a temporary DTO to validate
            SolutionDTO tempDTO = new SolutionDTO();
            tempDTO.setDriveLink(driveLink);
            
            response.put("valid", tempDTO.hasValidDriveLink());
            response.put("originalUrl", driveLink);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}