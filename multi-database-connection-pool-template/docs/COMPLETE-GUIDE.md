# Spring Boot Multiple Databases - Complete Guide
### The Ultimate Reference for Implementing 2 Databases in Spring Boot

> **Last Updated**: March 2026  
> **Difficulty**: Intermediate  
> **Time to Implement**: 30-45 minutes

---

# 📑 Table of Contents

- [Part 1: Quick Start (30 Second Answer)](#part-1-quick-start)
- [Part 2: Complete Implementation Guide](#part-2-complete-implementation-guide)
- [Part 3: Configuration Deep Dive](#part-3-configuration-deep-dive)
- [Part 4: Code Examples](#part-4-code-examples)
- [Part 5: Visual Architecture](#part-5-visual-architecture)
- [Part 6: Common Use Cases](#part-6-common-use-cases)
- [Part 7: Troubleshooting](#part-7-troubleshooting)
- [Part 8: Best Practices](#part-8-best-practices)
- [Part 9: Testing](#part-9-testing)
- [Part 10: Interview Q&A](#part-10-interview-qa)

---

# Part 1: Quick Start

## 🎯 The Answer in 30 Seconds

**Q: How does Spring Boot identify which database to use?**

**A: Package-based routing!**

```java
@EnableJpaRepositories(
    basePackages = "com.example.orders.repository",  // ← This package
    entityManagerFactoryRef = "primaryEntityManagerFactory"  // ← Uses this DB
)
```

Spring automatically routes queries based on **which package** your repository is in!

### The Core Concept

```
Repository Package Location
           ↓
Configuration Mapping
           ↓
EntityManagerFactory Selection
           ↓
DataSource Selection
           ↓
Database Query Execution

AUTOMATIC - NO MANUAL SELECTION NEEDED! 🎉
```

---

# Part 2: Complete Implementation Guide

## 📋 Overview

This guide demonstrates how to implement **2 databases** in a Spring Boot application:
- **Primary Database (MySQL)**: Stores Orders
- **Secondary Database (PostgreSQL)**: Stores Users

### Why Multiple Databases?
- **Microservices**: Different domains/bounded contexts
- **Legacy Systems**: Integrating with existing databases
- **Read/Write Separation**: Master-Slave architecture
- **Multi-tenancy**: Separate database per tenant
- **Reporting**: Separate analytics database

---

## 🔧 5-Step Implementation

### Step 1: Add Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- MySQL Driver (for Primary Database) -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- PostgreSQL Driver (for Secondary Database) -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Spring Boot Web (for REST APIs) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

---

### Step 2: Configure application.yml

```yaml
spring:
  application:
    name: multi-database-app

  # PRIMARY DATABASE (MySQL - Orders Database)
  # Note: Use 'jdbc-url' instead of 'url' when configuring multiple datasources
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/ordersdb?useSSL=false&serverTimezone=UTC
      username: root
      password: root123
      driver-class-name: com.mysql.cj.jdbc.Driver
      
      # HikariCP connection pool settings
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
        connection-timeout: 30000
        pool-name: PrimaryHikariPool
    
    # SECONDARY DATABASE (PostgreSQL - Users Database)
    secondary:
      jdbc-url: jdbc:postgresql://localhost:5432/usersdb
      username: postgres
      password: postgres123
      driver-class-name: org.postgresql.Driver
      
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
        connection-timeout: 30000
        pool-name: SecondaryHikariPool

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080
```

**⚠️ CRITICAL:** Use `jdbc-url` (not `url`) when configuring multiple datasources!

---

### Step 3: Create Primary Database Configuration

**File: `PrimaryDatabaseConfig.java`**

```java
package com.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.orders.repository",  // Package for Order repositories
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDatabaseConfig {

    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        
        return builder
                .dataSource(dataSource)
                .packages("com.example.orders.entity")  // Package for Order entities
                .persistenceUnit("primary")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") 
            LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
        return new JpaTransactionManager(primaryEntityManagerFactory.getObject());
    }
}
```

---

### Step 4: Create Secondary Database Configuration

**File: `SecondaryDatabaseConfig.java`**

```java
package com.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.users.repository",  // Package for User repositories
    entityManagerFactoryRef = "secondaryEntityManagerFactory",
    transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryDatabaseConfig {

    @Bean(name = "secondaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "secondaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("secondaryDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        
        return builder
                .dataSource(dataSource)
                .packages("com.example.users.entity")  // Package for User entities
                .persistenceUnit("secondary")
                .properties(properties)
                .build();
    }

    @Bean(name = "secondaryTransactionManager")
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") 
            LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory) {
        return new JpaTransactionManager(secondaryEntityManagerFactory.getObject());
    }
}
```

**Key Difference**: NO `@Primary` annotation on secondary database beans!

---

### Step 5: Organize Your Code

```
src/main/java/com/example/
├── config/
│   ├── PrimaryDatabaseConfig.java      ← MySQL configuration
│   └── SecondaryDatabaseConfig.java    ← PostgreSQL configuration
│
├── orders/                              ← PRIMARY DATABASE DOMAIN
│   ├── entity/
│   │   └── Order.java
│   └── repository/
│       └── OrderRepository.java
│
├── users/                               ← SECONDARY DATABASE DOMAIN
│   ├── entity/
│   │   └── User.java
│   └── repository/
│       └── UserRepository.java
│
└── service/
    └── MultiDatabaseService.java       ← Uses both databases

src/main/resources/
└── application.yml                      ← Database configurations
```

---

# Part 3: Configuration Deep Dive

## 🔑 Each Database Needs 3 Beans

| Bean | Purpose | Example Name |
|------|---------|--------------|
| **DataSource** | Connection pool to database | `primaryDataSource` |
| **EntityManagerFactory** | JPA entity management | `primaryEntityManagerFactory` |
| **TransactionManager** | Transaction handling | `primaryTransactionManager` |

### Bean Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Application Context                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  PRIMARY BEANS                    SECONDARY BEANS               │
│  ═════════════                    ════════════════              │
│                                                                 │
│  ┌─────────────────┐              ┌─────────────────┐          │
│  │ @Primary        │              │                 │          │
│  │ primaryData     │              │ secondaryData   │          │
│  │ Source          │              │ Source          │          │
│  └────────┬────────┘              └────────┬────────┘          │
│           │                                │                   │
│           │ injected into                  │ injected into     │
│           │                                │                   │
│  ┌────────▼────────┐              ┌───────▼─────────┐          │
│  │ @Primary        │              │                 │          │
│  │ primaryEntity   │              │ secondaryEntity │          │
│  │ ManagerFactory  │              │ ManagerFactory  │          │
│  └────────┬────────┘              └────────┬────────┘          │
│           │                                │                   │
│           │ manages                        │ manages           │
│           │                                │                   │
│  ┌────────▼────────┐              ┌───────▼─────────┐          │
│  │ Order entities  │              │ User entities   │          │
│  │ (orders pkg)    │              │ (users pkg)     │          │
│  └─────────────────┘              └─────────────────┘          │
│                                                                 │
│  ┌─────────────────┐              ┌─────────────────┐          │
│  │ @Primary        │              │                 │          │
│  │ primaryTx       │              │ secondaryTx     │          │
│  │ Manager         │              │ Manager         │          │
│  └────────┬────────┘              └────────┬────────┘          │
│           │                                │                   │
│  ┌────────▼────────┐              ┌───────▼─────────┐          │
│  │ OrderRepository │              │ UserRepository  │          │
│  └─────────────────┘              └─────────────────┘          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Package Structure Mapping

```
Project Structure                    Database Mapping
═══════════════════                  ════════════════

com.example
├── config
│   ├── PrimaryDatabaseConfig    →   [Configuration]
│   └── SecondaryDatabaseConfig  →   [Configuration]
│
├── orders                        →   PRIMARY DATABASE
│   ├── entity
│   │   └── Order.java           →   ordersdb.orders table
│   └── repository
│       └── OrderRepository      →   Queries ordersdb
│
└── users                         →   SECONDARY DATABASE
    ├── entity
    │   └── User.java            →   usersdb.users table
    └── repository
        └── UserRepository       →   Queries usersdb
```

---

# Part 4: Code Examples

## 📝 Entity Classes

### Order Entity (Primary Database)

```java
package com.example.orders.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;  // Reference to user in secondary database
    
    @Column(nullable = false, unique = true)
    private String orderNumber;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private String status;  // PENDING, CONFIRMED, SHIPPED, DELIVERED
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    // ... (see full code in Order.java)
}
```

### User Entity (Secondary Database)

```java
package com.example.users.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(name = "is_active")
    private boolean active;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        active = true;
    }
    
    // Getters and Setters
    // ... (see full code in User.java)
}
```

---

## 🗄️ Repository Interfaces

### OrderRepository (Primary Database)

```java
package com.example.orders.repository;

import com.example.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Automatically uses PRIMARY database (MySQL)
 * because it's in package "com.example.orders.repository"
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserId(Long userId);
    
    List<Order> findByStatus(String status);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status")
    List<Order> findUserOrdersByStatus(Long userId, String status);
    
    long countByStatus(String status);
    
    boolean existsByUserId(Long userId);
}
```

### UserRepository (Secondary Database)

```java
package com.example.users.repository;

import com.example.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Automatically uses SECONDARY database (PostgreSQL)
 * because it's in package "com.example.users.repository"
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findAllActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.active = true")
    Optional<User> findActiveUserByUsername(String username);
    
    List<User> findByIdIn(List<Long> ids);
}
```

---

## 🎯 Service Layer Examples

### Example 1: Query Single Database

```java
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;  // Uses PRIMARY database
    
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus("PENDING");
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
```

### Example 2: Query Both Databases

```java
@Service
public class MultiDatabaseService {
    
    @Autowired
    private OrderRepository orderRepository;  // PRIMARY database
    
    @Autowired
    private UserRepository userRepository;    // SECONDARY database
    
    /**
     * This method queries BOTH databases:
     * 1. User from SECONDARY database (PostgreSQL)
     * 2. Orders from PRIMARY database (MySQL)
     */
    public List<Order> getUserOrders(Long userId) {
        // Step 1: Query SECONDARY database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("Found user: " + user.getUsername());
        
        // Step 2: Query PRIMARY database
        List<Order> orders = orderRepository.findByUserId(user.getId());
        
        System.out.println("Found " + orders.size() + " orders");
        
        return orders;
    }
}
```

### Example 3: Write to Specific Database

```java
@Service
public class DataService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Write to PRIMARY database
     * Uses @Primary transaction manager by default
     */
    @Transactional
    public Order createOrder(Long userId, BigDecimal amount) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create order in PRIMARY database
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setTotalAmount(amount);
        order.setStatus("PENDING");
        
        return orderRepository.save(order);
    }
    
    /**
     * Write to SECONDARY database
     * Explicitly specify transaction manager
     */
    @Transactional(transactionManager = "secondaryTransactionManager")
    public User createUser(String username, String email, 
                          String firstName, String lastName) {
        
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        return userRepository.save(user);
    }
}
```

### Example 4: Complex Cross-Database Operations

```java
@Service
public class EcommerceService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * Get order details with user information
     * Joins data from both databases in application layer
     */
    public String getOrderDetailsWithUser(Long orderId) {
        // Get order from PRIMARY database
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Get user from SECONDARY database
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Join in application layer
        return String.format(
            "Order %s - Amount: $%.2f - Customer: %s (%s) - Status: %s",
            order.getOrderNumber(),
            order.getTotalAmount(),
            user.getFirstName() + " " + user.getLastName(),
            user.getEmail(),
            order.getStatus()
        );
    }
    
    /**
     * Batch operation: Get all orders with user information
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
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // Join in application layer
        return orders.stream()
                .map(order -> {
                    User user = userMap.get(order.getUserId());
                    if (user != null) {
                        return String.format("%s - %s - $%.2f", 
                            order.getOrderNumber(), 
                            user.getFirstName() + " " + user.getLastName(), 
                            order.getTotalAmount());
                    }
                    return String.format("%s - Unknown User - $%.2f", 
                        order.getOrderNumber(), 
                        order.getTotalAmount());
                })
                .collect(Collectors.toList());
    }
}
```

---

# Part 5: Visual Architecture

## 🏗️ Overall Architecture

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                     Spring Boot Application                           ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
          ┌─────────▼─────────┐      ┌─────────▼─────────┐
          │  OrderRepository  │      │  UserRepository   │
          │  (orders package) │      │  (users package)  │
          └─────────┬─────────┘      └─────────┬─────────┘
                    │                           │
          ┌─────────▼──────────┐     ┌─────────▼──────────┐
          │ PRIMARY            │     │ SECONDARY          │
          │ EntityManager      │     │ EntityManager      │
          │ Factory            │     │ Factory            │
          └─────────┬──────────┘     └─────────┬──────────┘
                    │                           │
          ┌─────────▼──────────┐     ┌─────────▼──────────┐
          │ PRIMARY            │     │ SECONDARY          │
          │ DataSource         │     │ DataSource         │
          │ (MySQL)            │     │ (PostgreSQL)       │
          └─────────┬──────────┘     └─────────┬──────────┘
                    │                           │
          ┌─────────▼──────────┐     ┌─────────▼──────────┐
          │  MySQL Database    │     │ PostgreSQL         │
          │  (ordersdb)        │     │ Database           │
          │                    │     │ (usersdb)          │
          │  Tables:           │     │ Tables:            │
          │  - orders          │     │ - users            │
          └────────────────────┘     └────────────────────┘
```

---

## 🔄 Request Flow: Query Single Database

### Scenario: Get all orders

```
1. Service Layer
   ┌──────────────────────────────────────┐
   │ orderRepository.findAll()            │
   └──────────────┬───────────────────────┘
                  │
2. Spring JPA     │
   ┌──────────────▼───────────────────────┐
   │ Check: Which package is              │
   │ OrderRepository in?                  │
   │ → com.example.orders.repository      │
   └──────────────┬───────────────────────┘
                  │
3. Configuration  │
   ┌──────────────▼───────────────────────┐
   │ Find config with basePackages:       │
   │ "com.example.orders.repository"      │
   │ → PrimaryDatabaseConfig              │
   └──────────────┬───────────────────────┘
                  │
4. Entity Manager │
   ┌──────────────▼───────────────────────┐
   │ Use: primaryEntityManagerFactory     │
   └──────────────┬───────────────────────┘
                  │
5. DataSource     │
   ┌──────────────▼───────────────────────┐
   │ Connect: primaryDataSource           │
   └──────────────┬───────────────────────┘
                  │
6. Database       │
   ┌──────────────▼───────────────────────┐
   │ Execute: SELECT * FROM orders        │
   │ Location: MySQL ordersdb             │
   └──────────────────────────────────────┘
```

---

## 🔀 Request Flow: Query Both Databases

```
┌─────────────────────────────────────────────────────────────┐
│ Service: getUserOrders(userId)                              │
└─────────────┬───────────────────────────────────────────────┘
              │
     ┌────────┴────────┐
     │                 │
┌────▼─────────┐  ┌───▼──────────┐
│ Query 1:     │  │ Query 2:     │
│ Find User    │  │ Find Orders  │
└────┬─────────┘  └───┬──────────┘
     │                │
┌────▼──────────────┐ │
│ userRepository    │ │
│ .findById()       │ │
└────┬──────────────┘ │
     │                │
┌────▼──────────────┐ │
│ SECONDARY         │ │
│ EntityManager     │ │
└────┬──────────────┘ │
     │                │
┌────▼──────────────┐ │
│ PostgreSQL        │ │
│ SELECT * FROM     │ │
│ users WHERE id=?  │ │
└────┬──────────────┘ │
     │                │
     │   ┌────────────▼──────────┐
     │   │ orderRepository       │
     │   │ .findByUserId()       │
     │   └────────┬──────────────┘
     │            │
     │   ┌────────▼──────────────┐
     │   │ PRIMARY               │
     │   │ EntityManager         │
     │   └────────┬──────────────┘
     │            │
     │   ┌────────▼──────────────┐
     │   │ MySQL                 │
     │   │ SELECT * FROM orders  │
     │   │ WHERE user_id = ?     │
     │   └────────┬──────────────┘
     │            │
     └────────────┴──────────────┐
                  │
        ┌─────────▼──────────────┐
        │ Combine results in     │
        │ Application Layer      │
        └────────────────────────┘
```

---

## 🎯 How Spring Identifies Database

```
                    Your Code
                       │
                       │ orderRepository.findAll()
                       │
                       ▼
        ┌──────────────────────────────┐
        │ Spring JPA Repository Proxy  │
        └──────────────┬───────────────┘
                       │
                       │ What package is this repository in?
                       ▼
        ┌──────────────────────────────┐
        │ Check Repository Package      │
        │ com.example.orders.repository │
        └──────────────┬───────────────┘
                       │
                       │ Find matching configuration
                       ▼
        ┌──────────────────────────────┐
        │ Scan all @EnableJpa          │
        │ Repositories configs         │
        │                              │
        │ Found: PrimaryDatabaseConfig │
        │   basePackages =             │
        │   "com.example.orders.repo"  │
        └──────────────┬───────────────┘
                       │
                       │ Use configured EntityManagerFactory
                       ▼
        ┌──────────────────────────────┐
        │ primaryEntityManagerFactory  │
        └──────────────┬───────────────┘
                       │
                       │ Connected to which DataSource?
                       ▼
        ┌──────────────────────────────┐
        │ primaryDataSource            │
        │ (MySQL Connection Pool)      │
        └──────────────┬───────────────┘
                       │
                       │ Execute query
                       ▼
        ┌──────────────────────────────┐
        │ MySQL Database               │
        │ ordersdb                     │
        └──────────────────────────────┘
```

---

## 🎯 Decision Tree: Which Database?

```
                Start
                  │
                  ▼
    ┌─────────────────────────┐
    │ Which Repository?       │
    └──────────┬──────────────┘
               │
        ┌──────┴──────┐
        │             │
        ▼             ▼
  OrderRepository  UserRepository
        │             │
        │             │
        ▼             ▼
   orders package  users package
        │             │
        │             │
        ▼             ▼
   PRIMARY Config  SECONDARY Config
        │             │
        │             │
        ▼             ▼
   primaryEMF     secondaryEMF
        │             │
        │             │
        ▼             ▼
   MySQL          PostgreSQL
   Database       Database
```

---

# Part 6: Common Use Cases

## 📊 Decision Matrix

| You Want To... | Use This | Example |
|----------------|----------|---------|
| Query orders database | `orderRepository` | `orderRepository.findAll()` |
| Query users database | `userRepository` | `userRepository.findAll()` |
| Create user | `userRepository.save()` | `userRepository.save(user)` |
| Create order | `orderRepository.save()` | `orderRepository.save(order)` |
| Get user's orders | Use both repositories | See Example 2 above |
| Rollback across both DBs | Use JTA (advanced) | Complex setup required |

---

## 💡 How Database Selection Works

```java
// When you call this:
orderRepository.findAll()

// Spring does this:
1. Check: OrderRepository is in "com.example.orders.repository"
2. Look up: Which config has basePackages = "com.example.orders.repository"?
3. Found: PrimaryDatabaseConfig
4. Use: primaryEntityManagerFactory
5. Route: Query goes to PRIMARY database (MySQL)
```

**No manual selection needed! Spring routes automatically!**

---

## 🔄 Transaction Flow

### Single Database Transaction

```
@Transactional  (uses @Primary by default)
┌────────────────────────────────────────┐
│ Start Transaction                      │
│ (primaryTransactionManager)            │
└─────────────┬──────────────────────────┘
              │
    ┌─────────▼─────────┐
    │ orderRepository   │
    │ .save(order)      │
    └─────────┬─────────┘
              │
    ┌─────────▼─────────┐
    │ INSERT INTO       │
    │ orders...         │
    │ (MySQL)           │
    └─────────┬─────────┘
              │
    ┌─────────▼─────────┐
    │ Commit            │
    └───────────────────┘
```

### Multiple Database Operations (NOT Distributed Transaction)

```
┌──────────────────────────────────────────────────────────────┐
│ Method: createUserAndOrder()                                 │
└────────────────┬─────────────────────────────────────────────┘
                 │
    ┌────────────▼────────────┐
    │ Transaction 1 START     │
    │ (secondaryTxManager)    │
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │ userRepository.save()   │
    │ → PostgreSQL            │
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │ Transaction 1 COMMIT ✓  │  ← User is now COMMITTED!
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │ Transaction 2 START     │
    │ (primaryTxManager)      │
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │ orderRepository.save()  │
    │ → MySQL                 │
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │ Transaction 2 COMMIT ✓  │
    └─────────────────────────┘

⚠️  If Transaction 2 fails, Transaction 1 is NOT rolled back!
    This is because they are separate transactions.
```

---

# Part 7: Troubleshooting

## 🐛 Common Errors & Fixes

### Error 1: "expected single matching bean but found 2"

**Full Error:**
```
expected single matching bean but found 2: primaryDataSource, secondaryDataSource
```

**Cause:** Spring doesn't know which datasource to use as default.

**Solution:** Add `@Primary` annotation to one datasource:
```java
@Primary
@Bean(name = "primaryDataSource")
public DataSource primaryDataSource() { ... }
```

---

### Error 2: "Not a managed type"

**Full Error:**
```
Not a managed type: class com.example.orders.entity.Order
```

**Cause:** Entity package not configured correctly.

**Solution:** Check entity package in configuration:
```java
builder
    .dataSource(dataSource)
    .packages("com.example.orders.entity")  // ← Verify this!
    .persistenceUnit("primary")
    .build();
```

---

### Error 3: "jdbcUrl is required with driverClassName"

**Full Error:**
```
jdbcUrl is required with driverClassName
```

**Cause:** Using `url` instead of `jdbc-url` in configuration.

**Solution:** Use `jdbc-url` in application.yml:
```yaml
# ❌ WRONG
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/ordersdb

# ✅ CORRECT
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/ordersdb
```

---

### Error 4: Wrong Database Being Queried

**Problem:** Repository queries wrong database

**Cause:** Package configuration mismatch

**Solution:** Verify `basePackages` matches your repository package:
```java
@EnableJpaRepositories(
    basePackages = "com.example.orders.repository",  // ← Verify this matches!
    entityManagerFactoryRef = "primaryEntityManagerFactory"
)
```

---

### Error 5: Transaction Not Rolling Back

**Problem:** Changes in one database not rolled back when other fails

**Cause:** Multiple datasources = separate transactions by default

**Solution:** This is expected behavior! Options:
1. Use JTA for distributed transactions
2. Implement Saga pattern
3. Design system to avoid cross-database transactions

---

## ⚠️ Critical Points

### ✅ DO's

- ✅ Use `jdbc-url` in application.yml (not `url`)
- ✅ Mark one datasource as `@Primary`
- ✅ Organize entities in separate packages
- ✅ Each database needs 3 beans (DataSource, EMF, TransactionManager)
- ✅ Test each database independently
- ✅ Use meaningful bean names
- ✅ Configure connection pools properly

### ❌ DON'Ts

- ❌ Don't use `url` in configuration (use `jdbc-url`)
- ❌ Don't try to join across databases in JPA queries
- ❌ Don't assume automatic distributed transactions
- ❌ Don't forget to specify different packages in configs
- ❌ Don't use same entity package for both databases

---

## 📋 Configuration Checklist

- [ ] Added both database drivers to pom.xml
- [ ] Configured datasources with `jdbc-url` in application.yml
- [ ] Created PrimaryDatabaseConfig class
- [ ] Created SecondaryDatabaseConfig class
- [ ] Marked one datasource as `@Primary`
- [ ] Organized entities in separate packages
- [ ] Set correct `basePackages` in `@EnableJpaRepositories`
- [ ] Created transaction managers for each database
- [ ] Tested both databases independently
- [ ] Added health checks for both databases

---

# Part 8: Best Practices

## 💡 Package Organization

```
src/main/java
└── com.example
    ├── config
    │   ├── PrimaryDatabaseConfig.java
    │   └── SecondaryDatabaseConfig.java
    ├── orders              ← Domain 1 (PRIMARY DB)
    │   ├── entity
    │   ├── repository
    │   └── service
    └── users               ← Domain 2 (SECONDARY DB)
        ├── entity
        ├── repository
        └── service
```

**Benefits:**
- Clear separation of concerns
- Easy to identify which database each component uses
- Follows Domain-Driven Design principles

---

## 🎯 Naming Conventions

### Good Naming
```java
// PRIMARY DATABASE
- primaryDataSource
- primaryEntityManagerFactory
- primaryTransactionManager

// SECONDARY DATABASE
- secondaryDataSource
- secondaryEntityManagerFactory
- secondaryTransactionManager
```

### Better Naming (Domain-Specific)
```java
// ORDERS DATABASE
- ordersDataSource
- ordersEntityManagerFactory
- ordersTransactionManager

// USERS DATABASE
- usersDataSource
- usersEntityManagerFactory
- usersTransactionManager
```

---

## 🚫 Avoid Cross-Database Joins

```java
// ❌ BAD: Cannot join across databases in SQL
@Query("SELECT o FROM Order o JOIN User u ON o.userId = u.id")
List<Order> findOrdersWithUsers();

// ✅ GOOD: Fetch separately and join in application layer
public List<OrderWithUser> getOrdersWithUsers() {
    // Step 1: Get orders from PRIMARY database
    List<Order> orders = orderRepository.findAll();
    
    // Step 2: Get user IDs
    List<Long> userIds = orders.stream()
            .map(Order::getUserId)
            .distinct()
            .collect(Collectors.toList());
    
    // Step 3: Batch fetch users from SECONDARY database
    List<User> users = userRepository.findByIdIn(userIds);
    Map<Long, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, u -> u));
    
    // Step 4: Join in Java
    return orders.stream()
            .map(order -> new OrderWithUser(
                order, 
                userMap.get(order.getUserId())
            ))
            .collect(Collectors.toList());
}
```

---

## ⚙️ Connection Pool Configuration

```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/ordersdb
      username: root
      password: root123
      hikari:
        maximum-pool-size: 20        # Max connections
        minimum-idle: 10             # Min idle connections
        connection-timeout: 30000    # 30 seconds
        idle-timeout: 600000         # 10 minutes
        max-lifetime: 1800000        # 30 minutes
        pool-name: PrimaryHikariPool
        
    secondary:
      jdbc-url: jdbc:postgresql://localhost:5432/usersdb
      username: postgres
      password: postgres123
      hikari:
        maximum-pool-size: 20
        minimum-idle: 10
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
        pool-name: SecondaryHikariPool
```

---

## 🏥 Health Checks

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    @Qualifier("primaryDataSource")
    private DataSource primaryDataSource;
    
    @Autowired
    @Qualifier("secondaryDataSource")
    private DataSource secondaryDataSource;
    
    @Override
    public Health health() {
        try (Connection conn1 = primaryDataSource.getConnection();
             Connection conn2 = secondaryDataSource.getConnection()) {
            
            boolean primary = conn1.isValid(1000);
            boolean secondary = conn2.isValid(1000);
            
            if (primary && secondary) {
                return Health.up()
                        .withDetail("primary", "UP")
                        .withDetail("secondary", "UP")
                        .build();
            } else {
                return Health.down()
                        .withDetail("primary", primary ? "UP" : "DOWN")
                        .withDetail("secondary", secondary ? "UP" : "DOWN")
                        .build();
            }
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

---

# Part 9: Testing

## 🧪 Unit Testing

### Test Individual Databases

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class MultiDatabaseIntegrationTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testPrimaryDatabase() {
        // Create order
        Order order = new Order();
        order.setUserId(1L);
        order.setOrderNumber("TEST-001");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus("PENDING");
        
        // Save to PRIMARY database
        Order saved = orderRepository.save(order);
        
        // Verify
        assertNotNull(saved.getId());
        assertEquals("TEST-001", saved.getOrderNumber());
    }
    
    @Test
    void testSecondaryDatabase() {
        // Create user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        
        // Save to SECONDARY database
        User saved = userRepository.save(user);
        
        // Verify
        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
    }
    
    @Test
    void testCrossDatabase() {
        // Create user in SECONDARY database
        User user = new User("john", "john@example.com", "John", "Doe");
        User savedUser = userRepository.save(user);
        
        // Create order in PRIMARY database
        Order order = new Order();
        order.setUserId(savedUser.getId());
        order.setOrderNumber("TEST-002");
        order.setTotalAmount(new BigDecimal("250.00"));
        order.setStatus("CONFIRMED");
        Order savedOrder = orderRepository.save(order);
        
        // Verify cross-database query
        List<Order> userOrders = orderRepository.findByUserId(savedUser.getId());
        assertEquals(1, userOrders.size());
        assertEquals("TEST-002", userOrders.get(0).getOrderNumber());
    }
}
```

---

### Test Configuration (application-test.yml)

```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:h2:mem:testordersdb
      username: sa
      password:
      driver-class-name: org.h2.Driver
    
    secondary:
      jdbc-url: jdbc:h2:mem:testusersdb
      username: sa
      password:
      driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

  h2:
    console:
      enabled: true
```

---

## 🔍 Quick Test Code

```java
@SpringBootTest
class QuickDatabaseTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void verifyPrimaryDatabase() {
        Order order = new Order();
        order.setOrderNumber("VERIFY-001");
        order.setUserId(1L);
        order.setTotalAmount(BigDecimal.TEN);
        order.setStatus("TEST");
        
        Order saved = orderRepository.save(order);
        assertNotNull(saved.getId());
        
        System.out.println("✓ PRIMARY Database Working!");
    }
    
    @Test
    void verifySecondaryDatabase() {
        User user = new User();
        user.setUsername("verify-user");
        user.setEmail("verify@test.com");
        user.setFirstName("Verify");
        user.setLastName("Test");
        
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        
        System.out.println("✓ SECONDARY Database Working!");
    }
}
```

---

# Part 10: Interview Q&A

## 🎓 Interview Questions & Answers

### Q1: How does Spring Boot identify which database to use?

**Answer:**

Spring Boot uses **package-based routing** configured via `@EnableJpaRepositories`:

1. Configuration class maps repository packages to specific `EntityManagerFactory`
2. When you use a repository, Spring checks which package it's in
3. Spring routes the query to the corresponding database automatically
4. Example: repositories in `com.example.orders.repository` → Primary Database

**No manual database selection needed!** The routing is automatic based on package structure.

---

### Q2: What are the required components for each database?

**Answer:**

Each database requires **3 beans**:

1. **DataSource**: Connection pool to the database
   ```java
   @Bean
   public DataSource primaryDataSource() { ... }
   ```

2. **EntityManagerFactory**: JPA entity management
   ```java
   @Bean
   public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory() { ... }
   ```

3. **TransactionManager**: Transaction handling
   ```java
   @Bean
   public PlatformTransactionManager primaryTransactionManager() { ... }
   ```

---

### Q3: Can you have transactions across multiple databases?

**Answer:**

**By default, NO.** Each database has its own transaction manager, so:

- Single database operations are transactional
- Cross-database operations are NOT automatically transactional
- If operation on DB2 fails, changes in DB1 are already committed

**Solutions:**
1. Use **JTA (Java Transaction API)** with Atomikos for distributed transactions
2. Implement **Saga pattern** for compensating transactions
3. Design system to avoid cross-database transactions
4. Use eventual consistency patterns

---

### Q4: What's the difference between 'url' and 'jdbc-url' in configuration?

**Answer:**

- `url`: Used when Spring Boot auto-configures **single datasource**
- `jdbc-url`: **Required** when manually configuring **multiple datasources**

When using `DataSourceBuilder.create().build()` with `@ConfigurationProperties`, you **must** use `jdbc-url`.

```yaml
# ❌ WRONG (for multiple datasources)
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/ordersdb

# ✅ CORRECT
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/ordersdb
```

---

### Q5: How do you handle cross-database joins?

**Answer:**

You **cannot** do database-level joins across different databases in JPA. Instead:

**❌ Won't Work:**
```java
@Query("SELECT o FROM Order o JOIN User u ON o.userId = u.id")
List<Order> findOrdersWithUsers();
```

**✅ Correct Approach:**
1. Fetch data from both databases separately
2. Join in the application layer using Java streams/loops
3. Use batch fetching to optimize performance

```java
// Fetch from both databases
List<Order> orders = orderRepository.findAll();
List<User> users = userRepository.findByIdIn(userIds);

// Join in application layer
Map<Long, User> userMap = users.stream()
    .collect(Collectors.toMap(User::getId, u -> u));
```

---

### Q6: What is the @Primary annotation used for?

**Answer:**

`@Primary` marks one datasource as the **default** when:
- No specific datasource is specified
- Spring needs to autowire a bean but finds multiple matches
- `@Transactional` is used without explicit `transactionManager`

**Required to avoid `NoUniqueBeanDefinitionException`.**

```java
@Primary
@Bean(name = "primaryDataSource")
public DataSource primaryDataSource() {
    return DataSourceBuilder.create().build();
}
```

**Only ONE datasource should have `@Primary` annotation!**

---

### Q7: How do you test multiple databases?

**Answer:**

**Approach 1: Use H2 in-memory databases**
```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:h2:mem:testordersdb
    secondary:
      jdbc-url: jdbc:h2:mem:testusersdb
```

**Approach 2: Use Testcontainers**
```java
@Testcontainers
class DatabaseTest {
    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8");
    
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:13");
}
```

**Test Strategy:**
1. Test each database independently
2. Test cross-database operations
3. Verify correct routing
4. Check transaction boundaries

---

### Q8: What are common pitfalls?

**Answer:**

**Common Mistakes:**

1. **Using `url` instead of `jdbc-url`**
   - Causes: `jdbcUrl is required with driverClassName` error

2. **Not marking any datasource as `@Primary`**
   - Causes: `NoUniqueBeanDefinitionException`

3. **Using same entity package for both databases**
   - Causes: Entity managed by wrong EntityManagerFactory

4. **Assuming distributed transactions**
   - Result: Data inconsistency when one operation fails

5. **Trying to join across databases in JPA**
   - Won't work: Cannot join tables from different databases

---

### Q9: How do you handle transaction rollback across databases?

**Answer:**

**Default Behavior:**
- No automatic rollback across databases
- Each database has separate transaction

**Solutions:**

**1. JTA (Java Transaction API):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jta-atomikos</artifactId>
</dependency>
```

**2. Saga Pattern:**
- Execute operations sequentially
- Implement compensating transactions
- Rollback manually if later steps fail

**3. Design to Avoid:**
- Avoid cross-database transactions
- Use eventual consistency
- Design for single-database operations

---

### Q10: What are the performance considerations?

**Answer:**

**Performance Factors:**

1. **Connection Pools:**
   - Configure HikariCP properly
   - Set appropriate pool sizes
   - Monitor connection usage

2. **Cross-Database Queries:**
   - Fetch from both databases = 2 round trips
   - Use batch fetching to minimize queries
   - Consider caching frequently accessed data

3. **Transaction Overhead:**
   - Separate transactions = less overhead
   - But no distributed consistency
   - Trade-off between consistency and performance

4. **Network Latency:**
   - Multiple databases = multiple network calls
   - Consider database colocation
   - Use async operations when possible

**Best Practices:**
- Batch fetch related data
- Use caching strategically
- Monitor query performance
- Optimize connection pools

---

## 📊 Comparison: Single vs Multiple Databases

| Aspect | Single Database | Multiple Databases |
|--------|----------------|-------------------|
| **Configuration** | Simple | Complex (need configs for each) |
| **Transactions** | ACID across all tables | Separate transactions |
| **Joins** | SQL joins work | Must join in application |
| **Consistency** | Strong consistency | Eventual consistency |
| **Scalability** | Limited | Better (independent scaling) |
| **Complexity** | Low | Medium to High |
| **Use Case** | Simple applications | Microservices, multi-tenant |

---

## 🔍 Real-World Scenarios

### Scenario 1: E-commerce Platform

**Setup:**
- **Primary DB (MySQL)**: Orders, Payments, Inventory
- **Secondary DB (PostgreSQL)**: Users, Authentication, Profiles

**Why:**
- Different scaling requirements
- Separate concerns (transactional vs user data)
- Different backup strategies

---

### Scenario 2: Multi-Tenant SaaS

**Setup:**
- **Primary DB**: Shared application data
- **Secondary DB (per tenant)**: Tenant-specific data

**Why:**
- Data isolation
- Tenant-specific scaling
- Easier compliance (data residency)

---

### Scenario 3: Legacy Integration

**Setup:**
- **Primary DB (New)**: Modern application data
- **Secondary DB (Legacy)**: Existing system data

**Why:**
- Cannot modify legacy database
- Gradual migration strategy
- Different database technologies

---

## 💾 Files Reference

### Configuration Files
1. **PrimaryDatabaseConfig.java** - MySQL configuration
2. **SecondaryDatabaseConfig.java** - PostgreSQL configuration
3. **application.yml** - Database connection settings
4. **pom.xml** - Maven dependencies

### Entity Files
5. **Order.java** - Order entity (PRIMARY database)
6. **User.java** - User entity (SECONDARY database)

### Repository Files
7. **OrderRepository.java** - Order queries (PRIMARY database)
8. **UserRepository.java** - User queries (SECONDARY database)

### Service Files
9. **MultiDatabaseService.java** - Service using both databases

---

## 🚀 Quick Setup Guide

### 1. Create Databases
```sql
-- MySQL
CREATE DATABASE ordersdb;

-- PostgreSQL
CREATE DATABASE usersdb;
```

### 2. Update Configuration
```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/ordersdb
      username: your_username
      password: your_password
    secondary:
      jdbc-url: jdbc:postgresql://localhost:5432/usersdb
      username: your_username
      password: your_password
```

### 3. Run Application
```bash
mvn clean install
mvn spring-boot:run
```

### 4. Test Endpoints
```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","firstName":"John","lastName":"Doe"}'

# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"orderNumber":"ORD-001","totalAmount":100.00,"status":"PENDING"}'

# Get user orders
curl http://localhost:8080/api/users/1/orders
```

---

## 📝 Summary

### Key Takeaways

1. ✅ **Package-based routing**: Different packages → different databases
2. ✅ **@Primary annotation**: Marks default database
3. ✅ **Automatic routing**: Spring routes based on repository package
4. ✅ **3 beans per database**: DataSource, EntityManagerFactory, TransactionManager
5. ✅ **Use `jdbc-url`**: Required for multiple datasources
6. ❌ **No cross-database joins**: Join in application layer
7. ❌ **No distributed transactions**: Each database has separate transaction
8. ❌ **No manual selection**: Spring handles routing automatically

---

### The Core Principle

```
Package Location → Configuration Mapping → Database Selection
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

com.example.orders.repository
        ↓
PrimaryDatabaseConfig (basePackages = "com.example.orders.repository")
        ↓
primaryEntityManagerFactory
        ↓
PRIMARY DATABASE (MySQL)
```

**Spring automatically routes queries based on which package your repository is in!**

---

## 🎉 Conclusion

Implementing multiple databases in Spring Boot is straightforward when you understand the **package-based routing** mechanism. Spring Boot automatically identifies which database to use based on which package your repository belongs to.

**No manual database selection needed!**

### Remember:
- Separate packages for different databases
- Configuration classes map packages to databases
- @Primary annotation for default database
- Use `jdbc-url` in configuration
- Join data in application layer, not in database

---

**Happy Coding! 🚀**

---

*This guide covers everything you need to know about implementing multiple databases in Spring Boot. For more examples, check the code files in this directory.*

