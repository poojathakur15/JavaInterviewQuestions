package com.example.users.repository;

import com.example.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository - Uses SECONDARY Database (PostgreSQL)
 * 
 * This repository will automatically use secondaryEntityManagerFactory
 * because it's in package "com.example.users.repository"
 * 
 * All queries executed through this repository will go to PostgreSQL database
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find all active users
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findAllActiveUsers();
    
    // Find active user by username
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.active = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);
    
    // Search users by first name or last name (case-insensitive)
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsersByName(@Param("searchTerm") String searchTerm);
    
    // Find users by email domain
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%@', :domain)")
    List<User> findUsersByEmailDomain(@Param("domain") String domain);
    
    // Count active users
    long countByActive(boolean active);
    
    // Find users by IDs (useful for batch operations)
    List<User> findByIdIn(List<Long> ids);
}

