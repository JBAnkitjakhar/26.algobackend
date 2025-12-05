// src/main/java/com/algoarena/service/course/CourseTopicService.java
package com.algoarena.service.course;

import com.algoarena.dto.course.CourseTopicDTO;
import com.algoarena.dto.course.CourseTopicNameDTO;
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
     * Get single topic by ID
     * CACHED: Individual topics are cached
     */
    @Cacheable(value = "courseTopic", key = "#id")
    public CourseTopicDTO getTopicById(String id) {
        CourseTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));

        CourseTopicDTO dto = CourseTopicDTO.fromEntity(topic);
        long docCount = docRepository.countByTopicId(topic.getId());
        dto.setDocsCount(docCount);

        return dto;
    }

    /**
     * Get PUBLIC topic names only (for regular users)
     * CACHED: Lightweight, only id + name
     */
    @Cacheable(value = "topicNamesPublic")
    public List<CourseTopicNameDTO> getPublicTopicNames() {
        List<CourseTopic> topics = topicRepository.findByIsPublicTrueOrderByDisplayOrderAsc();

        return topics.stream()
                .map(CourseTopicNameDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get ALL topic names (for admin)
     * CACHED: Lightweight, only id + name + isPublic
     */
    @Cacheable(value = "topicNamesAdmin")
    public List<CourseTopicNameDTO> getAllTopicNames() {
        List<CourseTopic> topics = topicRepository.findAllByOrderByDisplayOrderAsc();

        return topics.stream()
                .map(CourseTopicNameDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create new topic (Admin only)
     * EVICTS: All topic list caches
     */
    @Transactional
    @CacheEvict(value = { "topicNamesPublic", "topicNamesAdmin" }, allEntries = true)
    public CourseTopicDTO createTopic(CourseTopicDTO dto, User currentUser) {
        if (topicRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Topic with name '" + dto.getName() + "' already exists");
        }

        CourseTopic topic = new CourseTopic();
        topic.setName(dto.getName());
        topic.setDescription(dto.getDescription());
        topic.setDisplayOrder(dto.getDisplayOrder());
        topic.setIconUrl(dto.getIconUrl());
        topic.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true);
        topic.setCreatedById(currentUser.getId());
        topic.setCreatedByName(currentUser.getName());

        CourseTopic savedTopic = topicRepository.save(topic);

        CourseTopicDTO result = CourseTopicDTO.fromEntity(savedTopic);
        result.setDocsCount(0L);

        return result;
    }

    /**
     * Update existing topic (Admin only)
     * EVICTS: All related caches
     */
    @Transactional
    @CacheEvict(value = { "courseTopic", "topicNamesPublic", "topicNamesAdmin", "courseDocsList", "courseDoc" }, allEntries = true)
    public CourseTopicDTO updateTopic(String id, CourseTopicDTO dto, User currentUser) {
        CourseTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));

        if (!topic.getName().equalsIgnoreCase(dto.getName())) {
            if (topicRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("Topic with name '" + dto.getName() + "' already exists");
            }
        }

        topic.setName(dto.getName());
        topic.setDescription(dto.getDescription());
        topic.setDisplayOrder(dto.getDisplayOrder());
        topic.setIconUrl(dto.getIconUrl());
        topic.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true);

        CourseTopic updatedTopic = topicRepository.save(topic);

        CourseTopicDTO result = CourseTopicDTO.fromEntity(updatedTopic);
        long docCount = docRepository.countByTopicId(updatedTopic.getId());
        result.setDocsCount(docCount);

        return result;
    }

    /**
     * Toggle topic public/private status
     * EVICTS: All caches since visibility changed
     */
    @Transactional
    @CacheEvict(value = { "courseTopic", "courseDocsList", "topicNamesPublic",
            "topicNamesAdmin", "courseDoc" }, allEntries = true)
    public CourseTopicDTO toggleTopicVisibility(String id) {
        CourseTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));

        topic.setIsPublic(!topic.getIsPublic());
        CourseTopic updatedTopic = topicRepository.save(topic);

        CourseTopicDTO result = CourseTopicDTO.fromEntity(updatedTopic);
        long docCount = docRepository.countByTopicId(updatedTopic.getId());
        result.setDocsCount(docCount);

        return result;
    }

    /**
     * Delete topic (Admin only)
     * CASCADE: Deletes all docs and images
     * EVICTS: All caches
     */
    @Transactional
    @CacheEvict(value = { "courseTopic", "courseDocsList", "courseDoc", "topicNamesPublic",
            "topicNamesAdmin" }, allEntries = true)
    public void deleteTopic(String id) {
        CourseTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));

        List<CourseDoc> docs = docRepository.findByTopicIdOrderByDisplayOrderAsc(id);

        // System.out.println("Deleting topic '" + topic.getName() + "' with " + docs.size() + " documents");

        for (CourseDoc doc : docs) {
            if (doc.getImageUrls() != null && !doc.getImageUrls().isEmpty()) {
                // System.out.println("  Deleting " + doc.getImageUrls().size() + " images from doc: " + doc.getTitle());

                for (String imageUrl : doc.getImageUrls()) {
                    try {
                        String publicId = extractPublicIdFromUrl(imageUrl);
                        cloudinaryService.deleteImage(publicId);
                        // System.out.println("    ✓ Deleted image: " + publicId);
                    } catch (Exception e) {
                        System.err.println("    ✗ Failed to delete image " + imageUrl + ": " + e.getMessage());
                    }
                }
            }

            docRepository.delete(doc);
            // System.out.println("  ✓ Deleted document: " + doc.getTitle());
        }

        topicRepository.delete(topic);
        // System.out.println("✓ Topic deleted successfully");
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

    public TopicStatsDTO getTopicStats() {
        long totalTopics = topicRepository.count();
        long totalDocs = docRepository.count();

        return new TopicStatsDTO(totalTopics, totalDocs);
    }

    public static class TopicStatsDTO {
        private Long totalTopics;
        private Long totalDocuments;

        public TopicStatsDTO(Long totalTopics, Long totalDocuments) {
            this.totalTopics = totalTopics;
            this.totalDocuments = totalDocuments;
        }

        public Long getTotalTopics() {
            return totalTopics;
        }

        public void setTotalTopics(Long totalTopics) {
            this.totalTopics = totalTopics;
        }

        public Long getTotalDocuments() {
            return totalDocuments;
        }

        public void setTotalDocuments(Long totalDocuments) {
            this.totalDocuments = totalDocuments;
        }
    }
}