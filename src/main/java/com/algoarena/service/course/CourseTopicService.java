// src/main/java/com/algoarena/service/course/CourseTopicService.java
package com.algoarena.service.course;

import com.algoarena.dto.course.CourseTopicDTO;
import com.algoarena.model.CourseTopic;
import com.algoarena.model.CourseDoc;
import com.algoarena.model.User;
import com.algoarena.repository.CourseTopicRepository;
import com.algoarena.repository.CourseDocRepository;
import com.algoarena.service.file.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseTopicService {

    @Autowired
    private CourseTopicRepository topicRepository;

    @Autowired
    private CourseDocRepository docRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Get all topics with document count (for topic listing page)
     * CACHED for performance
     */
    @Cacheable(value = "courseTopicsList", key = "'all'")
    public List<CourseTopicDTO> getAllTopicsWithDocCount() {
        List<CourseTopic> topics = topicRepository.findAllByOrderByDisplayOrderAsc();
        
        return topics.stream()
                .map(topic -> {
                    CourseTopicDTO dto = CourseTopicDTO.fromEntity(topic);
                    // Get document count for this topic
                    long docCount = docRepository.countByTopic_Id(topic.getId());
                    dto.setDocsCount(docCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get single topic by ID
     */
    public CourseTopicDTO getTopicById(String id) {
        CourseTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
        
        CourseTopicDTO dto = CourseTopicDTO.fromEntity(topic);
        long docCount = docRepository.countByTopic_Id(topic.getId());
        dto.setDocsCount(docCount);
        
        return dto;
    }

    /**
     * Create new topic (Admin only)
     */
    @Transactional
    @CacheEvict(value = "courseTopicsList", allEntries = true)
    public CourseTopicDTO createTopic(CourseTopicDTO dto, User currentUser) {
        // Validate topic name uniqueness
        if (topicRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Topic with name '" + dto.getName() + "' already exists");
        }

        CourseTopic topic = new CourseTopic();
        topic.setName(dto.getName());
        topic.setDescription(dto.getDescription());
        topic.setDisplayOrder(dto.getDisplayOrder());
        topic.setIconUrl(dto.getIconUrl());
        topic.setCreatedBy(currentUser);

        CourseTopic savedTopic = topicRepository.save(topic);
        
        CourseTopicDTO result = CourseTopicDTO.fromEntity(savedTopic);
        result.setDocsCount(0L);
        
        return result;
    }

    /**
     * Update existing topic (Admin only)
     */
    @Transactional
    @CacheEvict(value = "courseTopicsList", allEntries = true)
    public CourseTopicDTO updateTopic(String id, CourseTopicDTO dto, User currentUser) {
        CourseTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));

        // Check name uniqueness if name is being changed
        if (!topic.getName().equalsIgnoreCase(dto.getName())) {
            if (topicRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("Topic with name '" + dto.getName() + "' already exists");
            }
        }

        topic.setName(dto.getName());
        topic.setDescription(dto.getDescription());
        topic.setDisplayOrder(dto.getDisplayOrder());
        topic.setIconUrl(dto.getIconUrl());

        CourseTopic updatedTopic = topicRepository.save(topic);
        
        CourseTopicDTO result = CourseTopicDTO.fromEntity(updatedTopic);
        long docCount = docRepository.countByTopic_Id(updatedTopic.getId());
        result.setDocsCount(docCount);
        
        return result;
    }

    /**
     * Delete topic (Admin only)
     * CASCADE: Also deletes ALL documents in this topic and their images
     */
    @Transactional
    @CacheEvict(value = {"courseTopicsList", "courseDocsList", "courseDoc"}, allEntries = true)
    public void deleteTopic(String id) {
        CourseTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));

        // Get all documents in this topic
        List<CourseDoc> docs = docRepository.findByTopic_IdOrderByDisplayOrderAsc(id);
        
        System.out.println("Deleting topic '" + topic.getName() + "' with " + docs.size() + " documents");

        // Delete all documents and their images
        for (CourseDoc doc : docs) {
            // Delete all images for this document
            if (doc.getImageUrls() != null && !doc.getImageUrls().isEmpty()) {
                System.out.println("  Deleting " + doc.getImageUrls().size() + " images from doc: " + doc.getTitle());
                
                for (String imageUrl : doc.getImageUrls()) {
                    try {
                        String publicId = extractPublicIdFromUrl(imageUrl);
                        cloudinaryService.deleteImage(publicId);
                        System.out.println("    ✓ Deleted image: " + publicId);
                    } catch (Exception e) {
                        System.err.println("    ✗ Failed to delete image " + imageUrl + ": " + e.getMessage());
                        // Continue with other images even if one fails
                    }
                }
            }
            
            // Delete the document
            docRepository.delete(doc);
            System.out.println("  ✓ Deleted document: " + doc.getTitle());
        }

        // Finally, delete the topic
        topicRepository.delete(topic);
        System.out.println("✓ Topic deleted successfully");
    }

    /**
     * Extract Cloudinary public ID from URL
     */
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

    /**
     * Get topic statistics
     */
    public TopicStatsDTO getTopicStats() {
        long totalTopics = topicRepository.count();
        long totalDocs = docRepository.count();
        
        return new TopicStatsDTO(totalTopics, totalDocs);
    }

    /**
     * DTO for topic statistics
     */
    public static class TopicStatsDTO {
        private Long totalTopics;
        private Long totalDocuments;

        public TopicStatsDTO(Long totalTopics, Long totalDocuments) {
            this.totalTopics = totalTopics;
            this.totalDocuments = totalDocuments;
        }

        public Long getTotalTopics() { return totalTopics; }
        public void setTotalTopics(Long totalTopics) { this.totalTopics = totalTopics; }

        public Long getTotalDocuments() { return totalDocuments; }
        public void setTotalDocuments(Long totalDocuments) { this.totalDocuments = totalDocuments; }
    }
}