package com.example.orders.repository;

import com.example.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository - Uses PRIMARY Database (MySQL)
 * 
 * This repository will automatically use primaryEntityManagerFactory
 * because it's in package "com.example.orders.repository"
 * 
 * All queries executed through this repository will go to MySQL database
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find all orders for a specific user
    List<Order> findByUserId(Long userId);
    
    // Find orders by status
    List<Order> findByStatus(String status);
    
    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Find orders for a user with specific status
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status")
    List<Order> findUserOrdersByStatus(@Param("userId") Long userId, @Param("status") String status);
    
    // Find orders created after a specific date
    @Query("SELECT o FROM Order o WHERE o.createdAt > :date")
    List<Order> findOrdersCreatedAfter(@Param("date") LocalDateTime date);
    
    // Find orders with total amount greater than specified value
    @Query("SELECT o FROM Order o WHERE o.totalAmount > :amount")
    List<Order> findOrdersWithAmountGreaterThan(@Param("amount") BigDecimal amount);
    
    // Count orders by status
    long countByStatus(String status);
    
    // Check if user has any orders
    boolean existsByUserId(Long userId);
    
    // Find user's orders ordered by creation date descending
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find pending orders for a user
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = 'PENDING'")
    List<Order> findPendingOrdersByUser(@Param("userId") Long userId);
}

