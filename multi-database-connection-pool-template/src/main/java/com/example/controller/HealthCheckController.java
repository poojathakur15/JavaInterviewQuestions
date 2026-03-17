package com.example.controller;

import com.example.orders.entity.Order;
import com.example.orders.repository.OrderRepository;
import com.example.users.entity.User;
import com.example.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Health Check Controller
 * 
 * Simple REST endpoints to verify the multi-database setup is working correctly.
 * This controller demonstrates basic CRUD operations on both databases.
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Health check endpoint
     * Tests connectivity to both databases
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test primary database (Orders - MySQL)
            long orderCount = orderRepository.count();
            health.put("primaryDatabase", Map.of(
                "name", "MySQL (Orders)",
                "status", "UP",
                "recordCount", orderCount
            ));
        } catch (Exception e) {
            health.put("primaryDatabase", Map.of(
                "name", "MySQL (Orders)",
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
        
        try {
            // Test secondary database (Users - PostgreSQL)
            long userCount = userRepository.count();
            health.put("secondaryDatabase", Map.of(
                "name", "PostgreSQL (Users)",
                "status", "UP",
                "recordCount", userCount
            ));
        } catch (Exception e) {
            health.put("secondaryDatabase", Map.of(
                "name", "PostgreSQL (Users)",
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
        
        return ResponseEntity.ok(health);
    }

    /**
     * Get all orders from primary database
     */
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    /**
     * Get all users from secondary database
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Create a test order in primary database
     */
    @PostMapping("/orders/test")
    public ResponseEntity<Order> createTestOrder() {
        Order order = new Order();
        order.setOrderNumber("TEST-" + System.currentTimeMillis());
        order.setUserId(1L);
        order.setTotalAmount(new java.math.BigDecimal("99.99"));
        order.setStatus("PENDING");
        
        Order saved = orderRepository.save(order);
        return ResponseEntity.ok(saved);
    }

    /**
     * Create a test user in secondary database
     */
    @PostMapping("/users/test")
    public ResponseEntity<User> createTestUser() {
        User user = new User();
        user.setUsername("testuser-" + System.currentTimeMillis());
        user.setEmail("test" + System.currentTimeMillis() + "@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    /**
     * Database statistics endpoint
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("orders", Map.of(
            "database", "MySQL",
            "totalRecords", orderRepository.count(),
            "sampleQuery", "SELECT COUNT(*) FROM orders"
        ));
        
        stats.put("users", Map.of(
            "database", "PostgreSQL",
            "totalRecords", userRepository.count(),
            "sampleQuery", "SELECT COUNT(*) FROM users"
        ));
        
        return ResponseEntity.ok(stats);
    }
}

