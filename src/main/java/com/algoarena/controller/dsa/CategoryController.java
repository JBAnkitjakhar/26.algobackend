// File: src/main/java/com/algoarena/controller/dsa/CategoryController.java

package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.CategoryDTO;
import com.algoarena.dto.dsa.CategoryMetadataDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * GET /api/categories
     * Get all categories with question IDs
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, CategoryDTO>> getAllCategories() {
        Map<String, CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }   

    /**
     * GET /api/categories/metadata
     * Get category metadata (id + name only) for admin dropdowns
     * No caching - fast enough for 50 categories
     * 
     * Response:
     * [
     *   { "id": "abc123", "name": "Arrays" },
     *   { "id": "def456", "name": "HashMap" },
     *   ...
     * ]
     */
    @GetMapping("/metadata")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryMetadataDTO>> getCategoriesMetadata() {
        List<CategoryMetadataDTO> metadata = categoryService.getCategoriesMetadata();
        return ResponseEntity.ok(metadata);
    }

    /**
     * GET /api/categories/{id}
     * Get single category by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable String id) {
        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/categories
     * Create new category
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            CategoryDTO createdCategory = categoryService.createCategory(categoryDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category created successfully");
            response.put("data", createdCategory);
            
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create category");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * PUT /api/categories/{id}
     * Update category name and/or displayOrder
     * 
     * Request body:
     * {
     *   "name": "Updated Name",       // optional
     *   "displayOrder": 5             // optional
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category updated successfully");
            response.put("data", updatedCategory);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update category");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * DELETE /api/categories/{id}
     * Delete category and all its questions (cascade)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable String id) {
        try {
            Map<String, Object> result = categoryService.deleteCategory(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete category");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
}