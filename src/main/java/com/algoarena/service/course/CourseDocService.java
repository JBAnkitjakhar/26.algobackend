// src/main/java/com/algoarena/service/course/CourseDocService.java
package com.algoarena.service.course;

import com.algoarena.dto.course.CourseDocDTO;
import com.algoarena.model.CourseDoc;
import com.algoarena.model.CourseTopic;
import com.algoarena.model.User;
import com.algoarena.repository.CourseDocRepository;
import com.algoarena.repository.CourseTopicRepository;
import com.algoarena.service.file.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseDocService {

    @Autowired
    private CourseDocRepository docRepository;

    @Autowired
    private CourseTopicRepository topicRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private static final long MAX_DOC_SIZE = 5 * 1024 * 1024L; // 5MB

    /**
     * Get all documents for a topic (WITHOUT full content - for listing)
     * CACHED for performance
     */
    @Cacheable(value = "courseDocsList", key = "#topicId")
    public List<CourseDocDTO> getDocsByTopic(String topicId) {
        // Verify topic exists
        topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));

        List<CourseDoc> docs = docRepository.findByTopic_IdOrderByDisplayOrderAsc(topicId);
        
        // Return DTOs WITHOUT content (for listing)
        return docs.stream()
                .map(CourseDocDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get single document WITH full content (for reading)
     */
    @Cacheable(value = "courseDoc", key = "#id")
    public CourseDocDTO getDocById(String id) {
        CourseDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        // Return DTO WITH full content
        return CourseDocDTO.fromEntityWithContent(doc);
    }

    /**
     * Create new document (Admin only)
     */
    @Transactional
    @CacheEvict(value = {"courseDocsList", "courseTopicsList"}, allEntries = true)
    public CourseDocDTO createDoc(CourseDocDTO dto, User currentUser) {
        // Verify topic exists
        CourseTopic topic = topicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + dto.getTopicId()));

        // Check title uniqueness within topic (FIXED METHOD NAME)
        if (docRepository.existsByTitleIgnoreCaseAndTopic_Id(dto.getTitle(), dto.getTopicId())) {
            throw new RuntimeException("Document with title '" + dto.getTitle() + "' already exists in this topic");
        }

        CourseDoc doc = new CourseDoc();
        doc.setTitle(dto.getTitle());
        doc.setTopic(topic);
        doc.setDisplayOrder(dto.getDisplayOrder());
        doc.setCreatedBy(currentUser);

        // Set content (HTML from frontend editor)
        if (dto.getContent() != null) {
            doc.setContent(dto.getContent());
        }

        // Set image URLs (for tracking and cleanup)
        if (dto.getImageUrls() != null) {
            doc.setImageUrls(dto.getImageUrls());
        } else {
            doc.setImageUrls(new ArrayList<>());
        }

        // Calculate and validate total size
        long totalSize = calculateDocumentSize(doc);
        if (totalSize > MAX_DOC_SIZE) {
            throw new RuntimeException("Document size (" + formatSize(totalSize) + 
                    ") exceeds maximum limit of " + formatSize(MAX_DOC_SIZE));
        }
        doc.setTotalSize(totalSize);

        CourseDoc savedDoc = docRepository.save(doc);
        return CourseDocDTO.fromEntityWithContent(savedDoc);
    }

    /**
     * Update existing document (Admin only)
     */
    @Transactional
    @CacheEvict(value = {"courseDocsList", "courseDoc", "courseTopicsList"}, allEntries = true)
    public CourseDocDTO updateDoc(String id, CourseDocDTO dto, User currentUser) {
        CourseDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        // Verify topic exists if changed
        if (!doc.getTopic().getId().equals(dto.getTopicId())) {
            CourseTopic newTopic = topicRepository.findById(dto.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found with id: " + dto.getTopicId()));
            doc.setTopic(newTopic);
        }

        // Check title uniqueness if title is being changed (FIXED METHOD NAME)
        if (!doc.getTitle().equalsIgnoreCase(dto.getTitle())) {
            if (docRepository.existsByTitleIgnoreCaseAndTopic_Id(dto.getTitle(), dto.getTopicId())) {
                throw new RuntimeException("Document with title '" + dto.getTitle() + "' already exists in this topic");
            }
        }

        doc.setTitle(dto.getTitle());
        doc.setDisplayOrder(dto.getDisplayOrder());

        // Update content
        if (dto.getContent() != null) {
            doc.setContent(dto.getContent());
        }

        // Update image URLs
        if (dto.getImageUrls() != null) {
            // Get old images for potential cleanup
            // List<String> oldImageUrls = doc.getImageUrls() != null ? new ArrayList<>(doc.getImageUrls()) : new ArrayList<>();
            
            doc.setImageUrls(dto.getImageUrls());
            
            // Optional: Delete images that are no longer used
            // (Images in oldImageUrls but not in new imageUrls)
            // Commented out for now - you can enable if needed
            /*
            for (String oldUrl : oldImageUrls) {
                if (!dto.getImageUrls().contains(oldUrl)) {
                    try {
                        String publicId = extractPublicIdFromUrl(oldUrl);
                        cloudinaryService.deleteImage(publicId);
                    } catch (Exception e) {
                        System.err.println("Failed to delete unused image: " + e.getMessage());
                    }
                }
            }
            */
        }

        // Calculate and validate total size
        long totalSize = calculateDocumentSize(doc);
        if (totalSize > MAX_DOC_SIZE) {
            throw new RuntimeException("Document size (" + formatSize(totalSize) + 
                    ") exceeds maximum limit of " + formatSize(MAX_DOC_SIZE));
        }
        doc.setTotalSize(totalSize);

        CourseDoc updatedDoc = docRepository.save(doc);
        return CourseDocDTO.fromEntityWithContent(updatedDoc);
    }

    /**
     * Delete document (Admin only)
     */
    @Transactional
    @CacheEvict(value = {"courseDocsList", "courseDoc", "courseTopicsList"}, allEntries = true)
    public void deleteDoc(String id) {
        CourseDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        // Delete all images from Cloudinary
        if (doc.getImageUrls() != null && !doc.getImageUrls().isEmpty()) {
            System.out.println("Deleting " + doc.getImageUrls().size() + " images from document: " + doc.getTitle());
            
            for (String imageUrl : doc.getImageUrls()) {
                try {
                    String publicId = extractPublicIdFromUrl(imageUrl);
                    cloudinaryService.deleteImage(publicId);
                    System.out.println("  ✓ Deleted image: " + publicId);
                } catch (Exception e) {
                    // Log error but continue deletion
                    System.err.println("  ✗ Failed to delete image " + imageUrl + ": " + e.getMessage());
                }
            }
        }

        docRepository.delete(doc);
        System.out.println("✓ Document deleted: " + doc.getTitle());
    }

    /**
     * Calculate total document size (content + metadata)
     */
    private long calculateDocumentSize(CourseDoc doc) {
        long totalSize = 0;

        // HTML content size
        if (doc.getContent() != null) {
            totalSize += doc.getContent().getBytes(StandardCharsets.UTF_8).length;
        }

        // Image URLs size (just the URLs, not actual image files)
        if (doc.getImageUrls() != null) {
            for (String url : doc.getImageUrls()) {
                if (url != null) {
                    totalSize += url.getBytes(StandardCharsets.UTF_8).length;
                }
            }
        }

        // Document metadata size
        if (doc.getTitle() != null) {
            totalSize += doc.getTitle().getBytes(StandardCharsets.UTF_8).length;
        }

        return totalSize;
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

    /**
     * Format size in human-readable format
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Get document statistics
     */
    public DocStatsDTO getDocStats(String topicId) {
        long totalDocs = topicId != null ? 
                docRepository.countByTopic_Id(topicId) : 
                docRepository.count();
        
        return new DocStatsDTO(totalDocs);
    }

    /**
     * DTO for document statistics
     */
    public static class DocStatsDTO {
        private Long totalDocuments;

        public DocStatsDTO(Long totalDocuments) {
            this.totalDocuments = totalDocuments;
        }

        public Long getTotalDocuments() { return totalDocuments; }
        public void setTotalDocuments(Long totalDocuments) { this.totalDocuments = totalDocuments; }
    }
}