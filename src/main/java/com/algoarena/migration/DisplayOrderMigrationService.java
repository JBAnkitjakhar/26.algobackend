// src/main/java/com/algoarena/migration/DisplayOrderMigrationService.java
package com.algoarena.migration;

import com.algoarena.model.Category;
import com.algoarena.model.Question;
import com.algoarena.model.QuestionLevel;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
// import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * ONE-TIME MIGRATION SERVICE
 * Automatically sets displayOrder for existing questions based on creation date
 * 
 * ⚠️ IMPORTANT: After running once, comment out @Component annotation to prevent re-running!
 * Or add a flag in application.properties: migration.display-order.enabled=false
 */
// @Component
@Order(1) // Run first on startup
public class DisplayOrderMigrationService implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n===========================================");
        System.out.println("🔄 DISPLAY ORDER MIGRATION STARTING...");
        System.out.println("===========================================\n");
        
        // Check if migration is needed
        long questionsWithoutDisplayOrder = questionRepository.countByDisplayOrderIsNull();
        
        if (questionsWithoutDisplayOrder == 0) {
            System.out.println("✅ All questions already have displayOrder assigned!");
            System.out.println("   No migration needed.\n");
            System.out.println("===========================================\n");
            return;
        }
        
        System.out.println("📊 Found " + questionsWithoutDisplayOrder + " questions without displayOrder");
        System.out.println("   Setting displayOrder based on creation date...\n");
        
        List<Category> categories = categoryRepository.findAll();
        int totalUpdated = 0;
        
        for (Category category : categories) {
            System.out.println("📁 Processing category: " + category.getName());
            
            // Process each level
            int easyCount = migrateLevel(category, QuestionLevel.EASY);
            int mediumCount = migrateLevel(category, QuestionLevel.MEDIUM);
            int hardCount = migrateLevel(category, QuestionLevel.HARD);
            
            int categoryTotal = easyCount + mediumCount + hardCount;
            if (categoryTotal > 0) {
                System.out.println("   ✓ Updated " + categoryTotal + " questions (" +
                                 "Easy: " + easyCount + ", " +
                                 "Medium: " + mediumCount + ", " +
                                 "Hard: " + hardCount + ")\n");
            } else {
                System.out.println("   - No questions needed update\n");
            }
            
            totalUpdated += categoryTotal;
        }
        
        System.out.println("===========================================");
        System.out.println("✅ MIGRATION COMPLETE!");
        System.out.println("📊 Total updated: " + totalUpdated + " questions");
        System.out.println("===========================================");
        System.out.println("⚠️  REMEMBER: Comment out @Component annotation");
        System.out.println("   in DisplayOrderMigrationService.java to prevent");
        System.out.println("   this migration from running again!");
        System.out.println("===========================================\n");
    }
    
    /**
     * Migrate questions for a specific category and level
     * @return Number of questions updated
     */
    private int migrateLevel(Category category, QuestionLevel level) {
        // Get all questions in this category and level
        List<Question> allQuestions = questionRepository.findByCategory_IdAndLevel(
            category.getId(), 
            level
        );
        
        if (allQuestions.isEmpty()) {
            return 0;
        }
        
        // Filter questions with null displayOrder
        List<Question> questionsToUpdate = allQuestions.stream()
            .filter(q -> q.getDisplayOrder() == null)
            .sorted(Comparator.comparing(Question::getCreatedAt)) // Sort by creation date (oldest first)
            .toList();
        
        if (questionsToUpdate.isEmpty()) {
            return 0;
        }
        
        // Find highest existing displayOrder (for questions that already have it)
        Integer maxOrder = allQuestions.stream()
            .map(Question::getDisplayOrder)
            .filter(order -> order != null)
            .max(Integer::compareTo)
            .orElse(0);
        
        // Assign displayOrder to questions without it (based on creation date)
        for (Question question : questionsToUpdate) {
            maxOrder++;
            question.setDisplayOrder(maxOrder);
            questionRepository.save(question);
            
            System.out.println("      ✓ " + level + " - Order " + maxOrder + ": " + 
                             question.getTitle().substring(0, Math.min(50, question.getTitle().length())) +
                             (question.getTitle().length() > 50 ? "..." : ""));
        }
        
        return questionsToUpdate.size();
    }
}