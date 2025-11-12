// src/main/java/com/algoarena/controller/course/CourseImageController.java
package com.algoarena.controller.course;

import com.algoarena.service.file.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/courses/images")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
public class CourseImageController {

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Upload image for course document (Admin only)
     * POST /api/courses/images
     * 
     * Used by admin during document creation/editing
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadCourseImage(
            @RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Upload to Cloudinary in 'courses' folder
            Map<String, Object> result = cloudinaryService.uploadCourseImage(file);

            response.put("success", true);
            response.put("data", result);
            response.put("message", "Course image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Validation failed");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Image upload failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete image from Cloudinary (Admin only)
     * DELETE /api/courses/images
     * 
     * Used when admin removes image during editing
     * or when cleaning up unused images
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteCourseImage(
            @RequestParam String imageUrl) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Image URL is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Extract public ID from URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            
            // Delete from Cloudinary
            Map<String, Object> result = cloudinaryService.deleteImage(publicId);

            response.put("success", true);
            response.put("data", result);
            response.put("message", "Course image deleted successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Invalid image URL");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Image deletion failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get image upload configuration
     * GET /api/courses/images/config
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getImageConfig() {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> imageConfig = new HashMap<>();
        imageConfig.put("maxSize", "2MB");
        imageConfig.put("maxSizeBytes", 2 * 1024 * 1024);
        imageConfig.put("allowedTypes", new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"});
        imageConfig.put("allowedExtensions", new String[]{".jpg", ".jpeg", ".png", ".gif", ".webp"});
        imageConfig.put("folder", "courses");

        config.put("success", true);
        config.put("data", imageConfig);

        return ResponseEntity.ok(config);
    }

    /**
     * Extract Cloudinary public ID from URL
     * Example: https://res.cloudinary.com/cloud/image/upload/v123/algoarena/courses/uuid.jpg
     * Returns: algoarena/courses/uuid
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            throw new IllegalArgumentException("Invalid Cloudinary URL");
        }

        try {
            // Find the position after "/upload/"
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                throw new IllegalArgumentException("Invalid Cloudinary URL format");
            }

            String afterUpload = imageUrl.substring(uploadIndex + 8); // "/upload/".length() = 8

            // Skip version if present (e.g., "v1234567890/")
            if (afterUpload.startsWith("v") && afterUpload.indexOf("/") > 0) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            // Remove file extension
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex > 0) {
                return afterUpload.substring(0, dotIndex);
            }

            return afterUpload;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract public ID from URL: " + e.getMessage());
        }
    }
}