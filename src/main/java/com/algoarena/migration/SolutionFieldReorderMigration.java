// src/main/java/com/algoarena/migration/SolutionFieldReorderMigration.java
package com.algoarena.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OPTIONAL MIGRATION: Reorder fields for better readability
 * 
 * This creates new documents with logical field ordering.
 * NOT required for functionality - just for cleaner database structure.
 * 
 * Run this AFTER SolutionDenormalizationMigration completes.
 * Then delete this file.
 */
// @Component
public class SolutionFieldReorderMigration implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎨 STARTING FIELD REORDERING MIGRATION (Optional)");
        System.out.println("=".repeat(80) + "\n");

        try {
            reorderFields();
            System.out.println("\n✅ FIELD REORDERING COMPLETED!");
            System.out.println("🗑️  You can now delete SolutionFieldReorderMigration.java\n");
        } catch (Exception e) {
            System.err.println("\n❌ MIGRATION FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reorderFields() {
        List<org.bson.Document> solutions = mongoTemplate.getCollection("solutions")
                .find()
                .into(new java.util.ArrayList<>());

        System.out.println("📊 Found " + solutions.size() + " solutions to reorder\n");

        int successCount = 0;

        for (org.bson.Document oldDoc : solutions) {
            try {
                String solutionId = oldDoc.getObjectId("_id").toString();
                
                // Create new document with desired field order
                Map<String, Object> orderedDoc = new LinkedHashMap<>();
                
                // STEP 1: ID and reference fields (top)
                orderedDoc.put("_id", oldDoc.get("_id"));
                orderedDoc.put("questionId", oldDoc.get("questionId"));
                
                // STEP 2: Content fields
                orderedDoc.put("content", oldDoc.get("content"));
                orderedDoc.put("driveLink", oldDoc.get("driveLink"));
                orderedDoc.put("youtubeLink", oldDoc.get("youtubeLink"));
                
                // STEP 3: Media fields
                orderedDoc.put("imageUrls", oldDoc.get("imageUrls"));
                orderedDoc.put("visualizerFileIds", oldDoc.get("visualizerFileIds"));
                orderedDoc.put("codeSnippet", oldDoc.get("codeSnippet"));
                
                // STEP 4: Metadata fields (bottom)
                orderedDoc.put("createdByName", oldDoc.get("createdByName"));
                orderedDoc.put("createdAt", oldDoc.get("createdAt"));
                orderedDoc.put("updatedAt", oldDoc.get("updatedAt"));
                orderedDoc.put("_class", oldDoc.get("_class"));
                
                // Replace the old document with the new ordered one
                org.bson.Document newDoc = new org.bson.Document(orderedDoc);
                mongoTemplate.getCollection("solutions").replaceOne(
                    new org.bson.Document("_id", oldDoc.get("_id")),
                    newDoc
                );
                
                successCount++;
                
                if (successCount <= 3) {
                    System.out.println("✅ Reordered solution: " + solutionId);
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error reordering document: " + e.getMessage());
            }
        }

        System.out.println("\n📊 Successfully reordered " + successCount + " solutions");
    }
}