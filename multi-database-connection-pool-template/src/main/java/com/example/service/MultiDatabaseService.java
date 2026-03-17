package com.example.service;

import com.example.orders.entity.Order;
import com.example.orders.repository.OrderRepository;
import com.example.users.entity.User;
import com.example.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service demonstrating how to use multiple databases
 * 
 * KEY CONCEPT: Spring Boot automatically routes queries to correct database
 * based on which repository you use:
 * - OrderRepository → Primary Database (MySQL)
 * - UserRepository → Secondary Database (PostgreSQL)
 * 
 * You don't need to manually specify which database to use!
 */
@Service
public class MultiDatabaseService {
    
    @Autowired
    private OrderRepository orderRepository;  // Automatically uses PRIMARY database
    
    @Autowired
    private UserRepository userRepository;    // Automatically uses SECONDARY database
    
    /**
     * Example 1: Query SECONDARY database (PostgreSQL - Users)
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
    
    /**
     * Example 2: Query PRIMARY database (MySQL - Orders)
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * Example 3: Query BOTH databases
     * 
     * This method demonstrates accessing both databases in a single operation:
     * 1. First queries SECONDARY database to verify user exists
     * 2. Then queries PRIMARY database to get user's orders
     * 
     * Note: This is NOT a distributed transaction!
     */
    public List<Order> getUserOrders(Long userId) {
        // Step 1: Query SECONDARY database (PostgreSQL)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("Found user: " + user.getFullName());
        
        // Step 2: Query PRIMARY database (MySQL)
        List<Order> orders = orderRepository.findByUserId(user.getId());
        
        System.out.println("Found " + orders.size() + " orders for user: " + user.getUsername());
        
        return orders;
    }
    
    /**
     * Example 4: Write to SECONDARY database
     * Uses @Transactional with secondaryTransactionManager
     */
    @Transactional(transactionManager = "secondaryTransactionManager")
    public User createUser(String username, String email, String firstName, String lastName) {
        // Validate username doesn't exist
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }
        
        // Validate email doesn't exist
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }
        
        // Create and save user to SECONDARY database (PostgreSQL)
        User user = new User(username, email, firstName, lastName);
        User savedUser = userRepository.save(user);
        
        System.out.println("Created user in SECONDARY database: " + savedUser);
        
        return savedUser;
    }
    
    /**
     * Example 5: Write to PRIMARY database
     * Uses @Transactional without explicit transactionManager (uses @Primary by default)
     */
    @Transactional  // Uses primaryTransactionManager by default
    public Order createOrder(Long userId, BigDecimal amount, String status) {
        // Verify user exists in SECONDARY database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Generate order number
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Create and save order to PRIMARY database (MySQL)
        Order order = new Order(user.getId(), orderNumber, amount, status);
        Order savedOrder = orderRepository.save(order);
        
        System.out.println("Created order in PRIMARY database: " + savedOrder);
        
        return savedOrder;
    }
    
    /**
     * Example 6: Complex operation across both databases
     * 
     * IMPORTANT: This is NOT a distributed transaction!
     * - If order creation fails, user is already committed in database
     * - If you need true distributed transactions, use JTA
     */
    public Order createUserAndOrder(String username, String email, 
                                   String firstName, String lastName, 
                                   BigDecimal orderAmount) {
        // Step 1: Create user in SECONDARY database (separate transaction)
        User user = createUser(username, email, firstName, lastName);
        
        // Step 2: Create order in PRIMARY database (separate transaction)
        Order order = createOrder(user.getId(), orderAmount, "PENDING");
        
        // Note: If step 2 fails, step 1 is already committed!
        // This is why distributed transactions are important for critical operations
        
        return order;
    }
    
    /**
     * Example 7: Get order with user details
     * Demonstrates joining data from multiple databases in application layer
     * (Cannot do database-level joins across different databases)
     */
    public String getOrderDetailsWithUser(Long orderId) {
        // Get order from PRIMARY database
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        // Get user from SECONDARY database
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + order.getUserId()));
        
        // Join data in application layer
        return String.format("Order %s - Amount: $%.2f - Customer: %s (%s) - Status: %s",
                order.getOrderNumber(),
                order.getTotalAmount(),
                user.getFullName(),
                user.getEmail(),
                order.getStatus());
    }
    
    /**
     * Example 8: Batch operation across databases
     * Get all orders with user information
     */
    public List<String> getAllOrdersWithUsers() {
        // Get all orders from PRIMARY database
        List<Order> orders = orderRepository.findAll();
        
        // Get unique user IDs
        List<Long> userIds = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // Batch fetch users from SECONDARY database
        List<User> users = userRepository.findByIdIn(userIds);
        
        // Create map for quick lookup
        var userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // Join in application layer
        return orders.stream()
                .map(order -> {
                    User user = userMap.get(order.getUserId());
                    if (user != null) {
                        return String.format("%s - %s - $%.2f", 
                                order.getOrderNumber(), 
                                user.getFullName(), 
                                order.getTotalAmount());
                    } else {
                        return String.format("%s - Unknown User - $%.2f", 
                                order.getOrderNumber(), 
                                order.getTotalAmount());
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Example 9: Update operations on different databases
     */
    @Transactional(transactionManager = "primaryTransactionManager")
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setStatus(newStatus);
        
        return orderRepository.save(order);  // Updates PRIMARY database
    }
    
    @Transactional(transactionManager = "secondaryTransactionManager")
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setActive(false);
        
        return userRepository.save(user);  // Updates SECONDARY database
    }
    
    /**
     * Example 10: Statistics from both databases
     */
    public String getSystemStatistics() {
        long totalUsers = userRepository.count();           // SECONDARY database
        long activeUsers = userRepository.countByActive(true);
        long totalOrders = orderRepository.count();         // PRIMARY database
        long pendingOrders = orderRepository.countByStatus("PENDING");
        
        return String.format(
                "System Statistics:%n" +
                "Total Users: %d (Active: %d)%n" +
                "Total Orders: %d (Pending: %d)",
                totalUsers, activeUsers, totalOrders, pendingOrders
        );
    }
}

