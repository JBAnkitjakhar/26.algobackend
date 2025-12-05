// src/main/java/com/algoarena/service/course/CourseDocService.java
package com.algoarena.service.course;

import com.algoarena.dto.course.CourseDocDTO;
import com.algoarena.model.CourseDoc;
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
     * Get docs for topic WITHOUT content (for listing)
     * CACHED: Multiple users benefit from same listing
     */
    @Cacheable(value = "courseDocsList", key = "#topicId")
    public List<CourseDocDTO> getDocsByTopic(String topicId) {
        topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));

        List<CourseDoc> docs = docRepository.findByTopicIdOrderByDisplayOrderAsc(topicId);

        return docs.stream()
                .map(CourseDocDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get single doc WITH content (for reading)
     * CACHED: Same doc viewed by multiple users
     */
    @Cacheable(value = "courseDoc", key = "#id")
    public CourseDocDTO getDocById(String id) {
        CourseDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        return CourseDocDTO.fromEntityWithContent(doc);
    }

    /**
     * Create new document (Admin only)
     * EVICTS: Docs list cache for this topic
     */
    @Transactional
    @CacheEvict(value = { "courseDocsList", "courseTopic" }, allEntries = true)
    public CourseDocDTO createDoc(CourseDocDTO dto, User currentUser) {
        topicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + dto.getTopicId()));

        if (docRepository.existsByTitleIgnoreCaseAndTopicId(dto.getTitle(), dto.getTopicId())) {
            throw new RuntimeException("Document with title '" + dto.getTitle() + "' already exists in this topic");
        }

        CourseDoc doc = new CourseDoc();
        doc.setTitle(dto.getTitle());
        doc.setTopicId(dto.getTopicId());
        doc.setDisplayOrder(dto.getDisplayOrder());
        doc.setCreatedById(currentUser.getId());
        doc.setCreatedByName(currentUser.getName());

        if (dto.getContent() != null) {
            doc.setContent(dto.getContent());
        }

        if (dto.getImageUrls() != null) {
            doc.setImageUrls(dto.getImageUrls());
        } else {
            doc.setImageUrls(new ArrayList<>());
        }

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
     * EVICTS: Specific doc cache + lists
     */
    @Transactional
    @CacheEvict(value = { "courseDocsList", "courseDoc" }, allEntries = true)
    public CourseDocDTO updateDoc(String id, CourseDocDTO dto, User currentUser) {
        CourseDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        if (!doc.getTopicId().equals(dto.getTopicId())) {
            topicRepository.findById(dto.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found with id: " + dto.getTopicId()));
            doc.setTopicId(dto.getTopicId());
        }

        if (!doc.getTitle().equalsIgnoreCase(dto.getTitle())) {
            if (docRepository.existsByTitleIgnoreCaseAndTopicId(dto.getTitle(), dto.getTopicId())) {
                throw new RuntimeException("Document with title '" + dto.getTitle() + "' already exists in this topic");
            }
        }

        doc.setTitle(dto.getTitle());
        doc.setDisplayOrder(dto.getDisplayOrder());

        if (dto.getContent() != null) {
            doc.setContent(dto.getContent());
        }

        if (dto.getImageUrls() != null) {
            doc.setImageUrls(dto.getImageUrls());
        }

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
     * EVICTS: All related caches
     */
    @Transactional
    @CacheEvict(value = { "courseDocsList", "courseDoc", "courseTopic" }, allEntries = true)
    public void deleteDoc(String id) {
        CourseDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        if (doc.getImageUrls() != null && !doc.getImageUrls().isEmpty()) {
            // System.out.println("Deleting " + doc.getImageUrls().size() + " images from document: " + doc.getTitle());

            for (String imageUrl : doc.getImageUrls()) {
                try {
                    String publicId = extractPublicIdFromUrl(imageUrl);
                    cloudinaryService.deleteImage(publicId);
                    // System.out.println("  ✓ Deleted image: " + publicId);
                } catch (Exception e) {
                    System.err.println("  ✗ Failed to delete image " + imageUrl + ": " + e.getMessage());
                }
            }
        }

        docRepository.delete(doc);
        // System.out.println("✓ Document deleted: " + doc.getTitle());
    }

    private long calculateDocumentSize(CourseDoc doc) {
        long totalSize = 0;

        if (doc.getContent() != null) {
            totalSize += doc.getContent().getBytes(StandardCharsets.UTF_8).length;
        }

        if (doc.getImageUrls() != null) {
            for (String url : doc.getImageUrls()) {
                if (url != null) {
                    totalSize += url.getBytes(StandardCharsets.UTF_8).length;
                }
            }
        }

        if (doc.getTitle() != null) {
            totalSize += doc.getTitle().getBytes(StandardCharsets.UTF_8).length;
        }

        return totalSize;
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            throw new IllegalArgumentException("Invalid Cloudinary URL");
        }

        try {
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                throw new IllegalArgumentException("Invalid Cloudinary URL format");
            }

            String afterUpload = imageUrl.substring(uploadIndex + 8);

            if (afterUpload.startsWith("v") && afterUpload.indexOf("/") > 0) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex > 0) {
                return afterUpload.substring(0, dotIndex);
            }

            return afterUpload;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract public ID from URL: " + e.getMessage());
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public DocStatsDTO getDocStats(String topicId) {
        long totalDocs = topicId != null ? docRepository.countByTopicId(topicId) : docRepository.count();

        return new DocStatsDTO(totalDocs);
    }

    public static class DocStatsDTO {
        private Long totalDocuments;

        public DocStatsDTO(Long totalDocuments) {
            this.totalDocuments = totalDocuments;
        }

        public Long getTotalDocuments() {
            return totalDocuments;
        }

        public void setTotalDocuments(Long totalDocuments) {
            this.totalDocuments = totalDocuments;
        }
    }

    /**
     * Move document to a different topic (Admin only)
     * EVICTS: All related caches since both topics are affected
     */
    @Transactional
    @CacheEvict(value = { "courseDocsList", "courseDoc", "courseTopic" }, allEntries = true)
    public CourseDocDTO moveDocToTopic(String docId, String newTopicId) {
        // Find the document
        CourseDoc doc = docRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + docId));

        // Verify the new topic exists
        if (!topicRepository.existsById(newTopicId)) {
            throw new RuntimeException("Target topic not found with id: " + newTopicId);
        }

        // Check if document is already in the target topic
        if (doc.getTopicId().equals(newTopicId)) {
            throw new RuntimeException("Document is already in the target topic");
        }

        String oldTopicId = doc.getTopicId();

        // Update the topic ID
        doc.setTopicId(newTopicId);

        CourseDoc updatedDoc = docRepository.save(doc);

        System.out.println("✓ Moved document '" + doc.getTitle() + "' from topic " +
                oldTopicId + " to " + newTopicId);

        return CourseDocDTO.fromEntityWithContent(updatedDoc);
    }
}