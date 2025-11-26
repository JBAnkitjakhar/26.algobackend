// src/main/java/com/algoarena/controller/admin/AdminController.java
package com.algoarena.controller.admin;
import java.util.HashMap;
import java.util.Map;
import com.algoarena.dto.admin.AdminOverviewDTO;
import com.algoarena.dto.admin.UserDTO;
import com.algoarena.dto.dsa.AdminQuestionSummaryDTO;
import com.algoarena.dto.dsa.AdminSolutionSummaryDTO;
import com.algoarena.dto.dsa.QuestionDTO;
import com.algoarena.model.User;
import com.algoarena.model.UserRole;
import com.algoarena.service.admin.AdminOverviewService;
import com.algoarena.service.admin.UserService;
import com.algoarena.service.dsa.QuestionService;
import com.algoarena.service.dsa.SolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

/**
 * Admin-specific controller for summary/lightweight endpoints
 * These endpoints return minimal data for listing views
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
public class AdminController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SolutionService solutionService;

    @Autowired
    private AdminOverviewService adminOverviewService;

    @Autowired
    private UserService userService;
 
    /**
     * NEW: Get admin overview statistics
     * Returns all dashboard statistics in a single call
     * 
     * @return AdminOverviewDTO with all statistics
     */
    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewDTO> getAdminOverview() {
        AdminOverviewDTO overview = adminOverviewService.getAdminOverview();
        return ResponseEntity.ok(overview);
    }

    /**
     * Get admin questions summary (lightweight, paginated)
     * Returns minimal data without full content
     * UPDATED: Now includes displayOrder field
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Page of AdminQuestionSummaryDTO
     */
    @GetMapping("/questions/summary")
    public ResponseEntity<Page<AdminQuestionSummaryDTO>> getAdminQuestionsSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminQuestionSummaryDTO> summaries = questionService.getAdminQuestionsSummary(pageable);

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<QuestionDTO> getAdminQuestionById(@PathVariable String id) {
        System.out.println("=== ADMIN ENDPOINT HIT ===");
        System.out.println("Question ID requested: " + id);

        try {
            QuestionDTO question = questionService.getAdminQuestionById(id);
            System.out.println("Question retrieved successfully: " + question.getTitle());
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            System.out.println("ERROR in AdminController: " + e.getClass().getName());
            System.out.println("ERROR Message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get admin solutions summary (lightweight, paginated)
     * Returns minimal data without full content
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Page of AdminSolutionSummaryDTO
     */
    @GetMapping("/solutions/summary")
    public ResponseEntity<Page<AdminSolutionSummaryDTO>> getAdminSolutionsSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminSolutionSummaryDTO> summaries = solutionService.getAdminSolutionsSummary(pageable);

        return ResponseEntity.ok(summaries);
    }

    /**
     * Get all users with pagination (Admin/SuperAdmin only)
     * GET /api/admin/users?page=0&size=20
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        try {
            Page<UserDTO> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /** 
     * Get specific user details
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            UserDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "User not found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

    /**
     * Get users by role with pagination
     * GET /api/admin/users/role/{role}?page=0&size=20
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @PathVariable String role,
            Pageable pageable) {
        try {
            UserRole userRole = UserRole.fromString(role);
            Page<UserDTO> users = userService.getUsersByRole(userRole, pageable);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Update user role (SuperAdmin can create admins, Primary SuperAdmin can do all)
     * PUT /api/admin/users/{userId}/role
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String userId,
            @RequestBody RoleUpdateRequest request,
            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            UserDTO updatedUser = userService.updateUserRole(userId, request.getRole(), currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User role updated successfully");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Role update failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(403).body(errorResponse);
        }
    }
    
    /**
     * Get role permissions matrix
     * GET /api/admin/users/permissions
     */
    @GetMapping("/users/permissions")
    public ResponseEntity<Map<String, Object>> getRolePermissions() {
        Map<String, Object> permissions = new HashMap<>();
        
        // Define what each role can do
        Map<String, Object> userPermissions = new HashMap<>();
        userPermissions.put("canCreateQuestions", false);
        userPermissions.put("canEditQuestions", false);
        userPermissions.put("canDeleteQuestions", false);
        userPermissions.put("canManageUsers", false);
        userPermissions.put("canChangeRoles", false);
        userPermissions.put("canAccessAdminPanel", false);
        
        Map<String, Object> adminPermissions = new HashMap<>();
        adminPermissions.put("canCreateQuestions", true);
        adminPermissions.put("canEditQuestions", true);
        adminPermissions.put("canDeleteQuestions", true);
        adminPermissions.put("canManageUsers", false);
        adminPermissions.put("canChangeRoles", false); // ZERO role management
        adminPermissions.put("canAccessAdminPanel", true);
        
        Map<String, Object> superAdminPermissions = new HashMap<>();
        superAdminPermissions.put("canCreateQuestions", true);
        superAdminPermissions.put("canEditQuestions", true);
        superAdminPermissions.put("canDeleteQuestions", true);
        superAdminPermissions.put("canManageUsers", true);
        superAdminPermissions.put("canChangeRoles", true); // Can create ADMIN
        superAdminPermissions.put("canAccessAdminPanel", true);
        superAdminPermissions.put("canManageSystemSettings", true);
        
        permissions.put("USER", userPermissions);
        permissions.put("ADMIN", adminPermissions);
        permissions.put("SUPERADMIN", superAdminPermissions);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("permissions", permissions);
        response.put("hierarchy", new String[]{"USER", "ADMIN", "SUPERADMIN", "PRIMARY_SUPERADMIN"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * Request DTO for role updates
     */
    public static class RoleUpdateRequest {
        private UserRole role;

        public RoleUpdateRequest() {}

        public RoleUpdateRequest(UserRole role) {
            this.role = role;
        }

        public UserRole getRole() {
            return role;
        }

        public void setRole(UserRole role) {
            this.role = role;
        }
    }
  
}