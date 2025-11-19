// src/main/java/com/algoarena/repository/UserRepository.java
package com.algoarena.repository;

import com.algoarena.model.User;
import com.algoarena.model.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Find user by email (can be null/empty for GitHub users with private email)
    Optional<User> findByEmail(String email);

    // Find user by Google ID (most reliable for Google users)
    Optional<User> findByGoogleId(String googleId);

    // Find user by GitHub ID (most reliable for GitHub users)
    Optional<User> findByGithubId(String githubId);

    // NEW: Find user by GitHub username (backup lookup)
    Optional<User> findByGithubUsername(String githubUsername);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find users by role
    List<User> findByRole(UserRole role);

    // Count users by role
    long countByRole(UserRole role);

    // Find all admin users
    @Query("{ 'role': { $in: ['ADMIN', 'SUPERADMIN'] } }")
    List<User> findAllAdmins();

    // Count total users
    @Query(value = "{}", count = true)
    long countAllUsers();

    // NEW: Count users who logged in within a time range
    long countByLastLoginBetween(LocalDateTime start, LocalDateTime end);
    
    // NEW: Count users created within a time range
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // NEW: Find users who logged in today
    @Query("{ 'lastLogin': { $gte: ?0, $lte: ?1 } }")
    List<User> findUsersLoggedInBetween(LocalDateTime start, LocalDateTime end);

    // Custom query to find user by OAuth provider info
    // This is useful but less reliable than direct provider ID lookups
    @Query("{ $or: [ { 'googleId': ?0 }, { 'githubId': ?0 }, { 'email': ?1 } ] }")
    Optional<User> findByOAuthInfo(String providerId, String email);
}