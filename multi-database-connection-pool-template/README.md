# Multi-Database Connection Pool Template

A production-ready Spring Boot template for configuring multiple database connection pools with environment profiles (Dev, QA, Prod).

## 📍 Quick Info

**Spring Boot:** 2.7.14  
**Java:** 11+

---

## 📋 Table of Contents

1. [Quick Start](#-quick-start)
2. [How Spring Boot Selects Configuration Files](#-how-spring-boot-selects-configuration-files)
3. [Environment Profiles](#-environment-profiles)
4. [Project Structure](#-project-structure)
5. [Configuration Guide](#️-configuration-guide)
6. [Usage Examples](#-usage-examples)
7. [Database Setup](#️-database-setup)
8. [Integration Options](#-integration-options)
9. [Troubleshooting](#-troubleshooting)
10. [Docker Support](#-docker-support)
11. [Customization](#-customization)
12. [Quick Reference](#-quick-reference)

---

## 🔍 How Spring Boot Selects Configuration Files

### Configuration File Loading Order

Spring Boot uses a **hierarchical configuration system**:

```
1. application.yml (BASE - always loaded first)
   ↓
2. application-{profile}.yml (PROFILE-SPECIFIC - loaded based on active profile)
   ↓
3. Profile settings OVERRIDE base settings
```

### Step-by-Step Process

**Step 1: Load Base Configuration**
```
Spring Boot loads: application.yml
- Reads common settings (application name, server port, default profile)
- Applies to ALL environments
```

**Step 2: Determine Active Profile**
```
Spring Boot checks (in order of priority):

1. Command line argument:
   --spring.profiles.active=qa

2. Environment variable:
   SPRING_PROFILES_ACTIVE=qa

3. application.yml setting:
   spring.profiles.active: dev  ← Default if nothing else specified
```

**Step 3: Load Profile-Specific Configuration**
```
Based on active profile, Spring Boot loads:

Profile = dev    →  application-dev.yml
Profile = qa     →  application-qa.yml
Profile = prod   →  application-prod.yml
Profile = dev-h2 →  application.yml (profile section)
```

**Step 4: Merge & Override**
```
Profile-specific settings OVERRIDE base settings:

application.yml:
  server.port: 8080
  logging.level.root: INFO

application-dev.yml:
  logging.level.root: DEBUG  ← This WINS for dev profile

Result when running with dev profile:
  server.port: 8080          (from base)
  logging.level.root: DEBUG  (from dev profile - overridden)
```

### Examples

#### Example 1: Running with Dev Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**What Spring Boot Loads:**
1. ✅ `application.yml` (base)
2. ✅ `application-dev.yml` (dev profile)
3. ❌ `application-qa.yml` (not loaded)
4. ❌ `application-prod.yml` (not loaded)

**Result:**
- Database: `ordersdb_dev` on localhost
- Pool size: 5 connections
- Logging: DEBUG level
- Schema: auto-update

#### Example 2: Running with QA Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=qa
```

**What Spring Boot Loads:**
1. ✅ `application.yml` (base)
2. ❌ `application-dev.yml` (not loaded)
3. ✅ `application-qa.yml` (qa profile)
4. ❌ `application-prod.yml` (not loaded)

**Result:**
- Database: `ordersdb_qa` on QA servers
- Pool size: 10 connections
- Logging: INFO level
- Schema: validate only

#### Example 3: Running with Prod Profile
```bash
java -jar app.jar --spring.profiles.active=prod
```

**What Spring Boot Loads:**
1. ✅ `application.yml` (base)
2. ❌ `application-dev.yml` (not loaded)
3. ❌ `application-qa.yml` (not loaded)
4. ✅ `application-prod.yml` (prod profile)

**Result:**
- Database: From environment variables
- Pool size: 20 connections
- Logging: WARN level (minimal)
- Schema: no modifications

#### Example 4: No Profile Specified
```bash
mvn spring-boot:run
```

**What Spring Boot Loads:**
1. ✅ `application.yml` (base)
2. ✅ `application-dev.yml` (dev is default from `spring.profiles.active: dev`)

**Result:**
- Uses dev profile settings (because it's set as default in application.yml)

### Configuration Priority (Highest to Lowest)

```
1. Command line arguments (highest priority)
   java -jar app.jar --spring.datasource.primary.url=jdbc:mysql://custom

2. Environment variables
   export DB_PRIMARY_URL=jdbc:mysql://custom

3. Profile-specific YAML (application-{profile}.yml)
   spring.datasource.primary.url: jdbc:mysql://qa-server

4. Base YAML (application.yml) (lowest priority)
   spring.datasource.primary.url: jdbc:mysql://localhost
```

### Visual Example

```
application.yml:
┌─────────────────────────────────┐
│ Common Settings (ALL profiles) │
│ - application.name              │
│ - server.port: 8080             │
│ - spring.profiles.active: dev   │ ← Default profile
└─────────────────────────────────┘
              ↓
    Active Profile: dev
              ↓
┌─────────────────────────────────┐
│ application-dev.yml             │
│ - ordersdb_dev                  │
│ - pool: 5                       │
│ - logging: DEBUG                │
└─────────────────────────────────┘
              ↓
         MERGED RESULT:
┌─────────────────────────────────┐
│ Final Configuration             │
│ - application.name: ...         │ (from base)
│ - server.port: 8080             │ (from base)
│ - database: ordersdb_dev        │ (from dev)
│ - pool: 5                       │ (from dev)
│ - logging: DEBUG                │ (from dev)
└─────────────────────────────────┘
```

### How to Verify Which Profile is Active

**Check Startup Logs:**
```
========================================================================
  Multi-Database Application Started Successfully!
========================================================================
  Application:   multi-database-demo-dev
  Profile(s):    [dev]  ← Active profile shown here
  Local URL:     http://localhost:8080/
========================================================================
```

**Check Actuator Endpoint:**
```bash
curl http://localhost:8080/actuator/env | grep "spring.profiles.active"

# Response:
{
  "name": "spring.profiles.active",
  "value": "dev"
}
```

**Enable Spring Boot Debug:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Ddebug

# Logs will show:
# Loaded config file: application.yml
# Loaded config file: application-dev.yml
```

### Common Mistakes & Best Practices

❌ **Mistake 1: Defining same property in multiple files unnecessarily**
```yaml
# application.yml
spring.datasource.primary.jdbc-url: jdbc:mysql://localhost

# application-dev.yml
spring.datasource.primary.jdbc-url: jdbc:mysql://localhost:3306/ordersdb_dev

# Result: Profile-specific WINS (correct behavior)
# But better to ONLY define in profile-specific files
```

❌ **Mistake 2: Forgetting to set profile for production**
```bash
java -jar app.jar  # Uses default profile (dev) - DANGEROUS in prod!
```

❌ **Mistake 3: Typo in profile name**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=Dev  # Wrong! (case-sensitive)
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # Correct
```

✅ **Best Practice: Keep base configuration minimal**
```yaml
# application.yml - ONLY common settings
spring:
  application:
    name: multi-database-demo
  profiles:
    active: dev
server:
  port: 8080

# application-dev.yml - ALL dev-specific settings (including databases)
# application-qa.yml - ALL qa-specific settings (including databases)
# application-prod.yml - ALL prod-specific settings (including databases)
```

---

## 📋 Table of Contents

1. [Quick Start](#-quick-start)
2. [Environment Profiles](#-environment-profiles)
3. [Project Structure](#-project-structure)
4. [Configuration Guide](#️-configuration-guide)
5. [Usage Examples](#-usage-examples)
6. [Database Setup](#️-database-setup)
7. [Integration Options](#-integration-options)
8. [Troubleshooting](#-troubleshooting)
9. [Docker Support](#-docker-support)
10. [Customization](#-customization)
11. [Quick Reference](#-quick-reference)

---

## 🔧 Understanding the Three Core Components

### Overview: DataSource, EntityManagerFactory, and TransactionManager

Every database configuration class (`PrimaryDatabaseConfig`, `SecondaryDatabaseConfig`) creates three essential beans that work together:

```
┌─────────────────┐
│  DataSource     │  "The Connection Pool Manager"
└────────┬────────┘
         │ provides connections to
         ↓
┌─────────────────┐
│ EntityManager   │  "The JPA Entity Manager"
│   Factory       │
└────────┬────────┘
         │ managed by
         ↓
┌─────────────────┐
│ Transaction     │  "The Transaction Coordinator"
│   Manager       │
└─────────────────┘
```

---

### 1️⃣ DataSource - The Connection Pool Manager

**What It Is:**
- A **pool of database connections** (managed by HikariCP in this template)
- Think of it as a "parking lot" of ready-to-use database connections

**What It Does:**
- Creates and maintains multiple connections to the database
- Reuses connections instead of creating new ones (expensive operation)
- Manages connection lifecycle (open, close, validate, recycle)
- Handles connection pooling configuration (max size, timeout, etc.)

**Configuration Example:**
```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/ordersdb_dev
      username: dev_user
      password: dev_password
      hikari:
        maximum-pool-size: 5      # Keep 5 connections ready
        minimum-idle: 2            # At least 2 always available
        connection-timeout: 30000  # Wait 30s for connection
```

**In Code:**
```java
@Bean(name = "primaryDataSource")
@ConfigurationProperties(prefix = "spring.datasource.primary")
public DataSource primaryDataSource() {
    return DataSourceBuilder.create().build();
    // Creates HikariCP connection pool with 5 connections to MySQL
}
```

**Analogy:**
```
Without Connection Pool (Bad):
Every query creates new connection → Slow! (200-500ms overhead)

With DataSource (Good):
┌─────────────────────────────────┐
│ Connection Pool (5 connections) │
│ [C1] [C2] [C3] [C4] [C5]       │  ← Always ready
└─────────────────────────────────┘
Query comes in → Grabs C1 → Executes → Returns C1 → Fast! (0-5ms overhead)
```

**Real Example:**
```java
// When you call this:
orderRepository.findById(1L);

// Behind the scenes:
// 1. DataSource provides a connection from the pool
// 2. Query executes: SELECT * FROM orders WHERE id = 1
// 3. Connection returns to pool (not closed, reused!)
```

---

### 2️⃣ EntityManagerFactory - The JPA Entity Manager

**What It Is:**
- A **factory that creates EntityManager instances**
- EntityManager is JPA's main interface for database operations
- Connects your Java objects (entities) to database tables

**What It Does:**
- Scans packages for `@Entity` classes (like `Order.java`, `User.java`)
- Creates mappings between Java classes and database tables
- Converts Java objects to SQL and vice versa
- Manages entity lifecycle (new, managed, detached, removed)
- Handles JPA queries and operations

**Configuration Example:**
```java
@Bean(name = "primaryEntityManagerFactory")
public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("primaryDataSource") DataSource dataSource) {
    
    return builder
            .dataSource(dataSource)           // Uses this connection pool
            .packages("com.example.orders.entity")  // Scans for @Entity classes
            .persistenceUnit("primary")       // Logical name
            .properties(hibernateProperties)  // Hibernate settings
            .build();
}
```

**What It Scans:**
```java
// Scans this package:
com.example.orders.entity/
  └── Order.java

// Finds this class:
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    private BigDecimal totalAmount;
}

// Creates mapping:
Order Java Class ↔ orders Database Table
  id field       ↔ id column
  userId field   ↔ user_id column
  totalAmount    ↔ total_amount column
```

**Analogy:**
```
EntityManagerFactory = Blueprint Factory

1. Reads @Entity classes (Order.java)
2. Creates blueprints (metadata) for database operations
3. Produces EntityManager instances when needed
4. Each EntityManager knows how to:
   - Convert Order object → SQL INSERT
   - Convert SQL result → Order object
   - Track changes to objects
   - Generate UPDATE/DELETE SQL
```

**Real Example:**
```java
// When you call this:
Order order = new Order();
order.setUserId(1L);
order.setTotalAmount(new BigDecimal("99.99"));
orderRepository.save(order);

// EntityManagerFactory's EntityManager:
// 1. Detects: New Order object
// 2. Generates SQL: INSERT INTO orders (user_id, total_amount, ...) VALUES (1, 99.99, ...)
// 3. Uses DataSource to get connection
// 4. Executes SQL
// 5. Returns generated ID to Order object
// 6. Marks object as "managed" (tracks future changes)
```

**Entity Lifecycle Managed by EntityManager:**
```
NEW (order = new Order())
  ↓ orderRepository.save(order)
MANAGED (tracked by EntityManager)
  ↓ order.setStatus("SHIPPED")
  ↓ EntityManager detects change
  ↓ Transaction commits
UPDATED (UPDATE orders SET status='SHIPPED' WHERE id=1)
  ↓ Transaction ends
DETACHED (no longer tracked)
```

---

### 3️⃣ TransactionManager - The Transaction Coordinator

**What It Is:**
- Manages **database transactions** (BEGIN, COMMIT, ROLLBACK)
- Ensures data consistency and ACID properties
- Coordinates operations across multiple database calls

**What It Does:**
- Starts transactions when `@Transactional` method is called
- Commits transaction if method succeeds
- Rolls back transaction if exception occurs
- Manages transaction boundaries and isolation levels
- Ensures "all or nothing" for grouped operations

**Configuration Example:**
```java
@Bean(name = "primaryTransactionManager")
public PlatformTransactionManager primaryTransactionManager(
        @Qualifier("primaryEntityManagerFactory") 
        LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
    return new JpaTransactionManager(primaryEntityManagerFactory.getObject());
}
```

**In Your Code:**
```java
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    // TransactionManager handles this method
    @Transactional
    public void createOrder(Long userId, BigDecimal amount) {
        // Transaction starts here (BEGIN)
        
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(amount);
        orderRepository.save(order);  // INSERT
        
        order.setStatus("CONFIRMED");
        orderRepository.save(order);  // UPDATE
        
        // If exception thrown anywhere above → ROLLBACK
        // If method completes successfully → COMMIT
    }
}
```

**Analogy:**
```
TransactionManager = Database Safety Net

Without Transaction:
  Step 1: Insert order → ✓ Success
  Step 2: Update status → ✗ FAILS
  Result: Half-done operation! Order exists without status (BAD!)

With TransactionManager:
  BEGIN TRANSACTION
    Step 1: Insert order → ✓ Success (not committed yet)
    Step 2: Update status → ✗ FAILS
  ROLLBACK TRANSACTION
  Result: Nothing saved! Order doesn't exist (SAFE!)
```

**Real Example:**
```java
@Transactional
public void transferOrder(Long fromUserId, Long toUserId, Long orderId) {
    // TransactionManager: BEGIN
    
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.setUserId(toUserId);
    orderRepository.save(order);  // UPDATE orders SET user_id = toUserId
    
    // Simulate error
    if (someValidation()) {
        throw new RuntimeException("Transfer failed!");
    }
    
    // TransactionManager: ROLLBACK (due to exception)
    // Order still belongs to fromUserId (safe!)
}
```

**Transaction Flow:**
```
@Transactional Method Called
         ↓
┌────────────────────────────┐
│ TransactionManager         │
│ - Gets connection from     │
│   DataSource               │
│ - Executes: BEGIN          │
│ - Binds to current thread  │
└────────────────────────────┘
         ↓
┌────────────────────────────┐
│ Your Code Executes         │
│ - orderRepository.save()   │
│ - Multiple DB operations   │
└────────────────────────────┘
         ↓
    Success? ────No──→ ROLLBACK (undo everything)
         │
        Yes
         ↓
      COMMIT (save everything)
         ↓
   Transaction End
```

---

### How They Work Together

**Complete Flow:**

```java
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional  // ← TransactionManager takes over
    public Order createOrder(Long userId, BigDecimal amount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(amount);
        return orderRepository.save(order);
    }
}
```

**Step-by-Step Execution:**

```
1. Method Call: createOrder(1L, 99.99)
   ↓

2. TransactionManager Intercepts (@Transactional detected)
   ├─ Gets connection from DataSource
   ├─ Executes: BEGIN TRANSACTION
   └─ Passes control to method
   ↓

3. orderRepository.save(order) called
   ├─ EntityManagerFactory's EntityManager takes over
   ├─ Generates SQL: INSERT INTO orders (user_id, total_amount) VALUES (1, 99.99)
   ├─ Uses connection from DataSource
   └─ Executes SQL
   ↓

4. Method Completes Successfully
   ↓

5. TransactionManager Commits
   ├─ Executes: COMMIT
   ├─ Returns connection to DataSource pool
   └─ Transaction complete
   ↓

6. Return Order object to caller
```

**If Exception Occurs:**

```
1-3. Same as above
   ↓

4. Exception Thrown! (e.g., ValidationException)
   ↓

5. TransactionManager Catches Exception
   ├─ Executes: ROLLBACK
   ├─ All changes undone (INSERT never happened)
   ├─ Returns connection to DataSource pool
   └─ Re-throws exception
   ↓

6. Exception propagates to caller
```

---

### Visual Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Your Application                          │
│                                                                  │
│  @Service                                                        │
│  public class OrderService {                                     │
│      @Autowired                                                  │
│      private OrderRepository orderRepository; ─────┐             │
│                                                     │             │
│      @Transactional                                 │             │
│      public Order createOrder() {                   │             │
│          return orderRepository.save(order); ───────┼────────┐   │
│      }                                              │        │   │
│  }                                                  │        │   │
└─────────────────────────────────────────────────────┼────────┼───┘
                                                      │        │
                        ┌─────────────────────────────┘        │
                        ↓                                      │
┌─────────────────────────────────────────────┐                │
│      PrimaryDatabaseConfig                   │                │
│                                              │                │
│  ┌────────────────────────────────────────┐ │                │
│  │ 1. DataSource (HikariCP)               │ │                │
│  │    - Pool of 5 connections             │◄├────────────────┤
│  │    - jdbc:mysql://localhost/ordersdb   │ │                │
│  └────────────────────────────────────────┘ │                │
│                     ↓ provides connections   │                │
│  ┌────────────────────────────────────────┐ │                │
│  │ 2. EntityManagerFactory                │ │                │
│  │    - Maps: Order.java ↔ orders table   │◄├────────────────┘
│  │    - Generates SQL                     │ │
│  │    - Manages entities                  │ │
│  └────────────────────────────────────────┘ │
│                     ↓ managed by             │
│  ┌────────────────────────────────────────┐ │
│  │ 3. TransactionManager                  │ │
│  │    - BEGIN transaction                 │ │
│  │    - COMMIT or ROLLBACK                │ │
│  │    - Ensures data consistency          │ │
│  └────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│          MySQL Database                      │
│                                              │
│  orders table:                               │
│  ┌────┬─────────┬──────────────┬─────────┐  │
│  │ id │ user_id │ total_amount │ status  │  │
│  ├────┼─────────┼──────────────┼─────────┤  │
│  │ 1  │ 100     │ 99.99        │ PENDING │  │
│  └────┴─────────┴──────────────┴─────────┘  │
└─────────────────────────────────────────────┘
```

---

### Configuration Mapping

**In application-dev.yml:**
```yaml
spring:
  datasource:
    primary:                        # ← Read by DataSource
      jdbc-url: jdbc:mysql://localhost:3306/ordersdb_dev
      username: dev_user
      password: dev_password
      hikari:
        maximum-pool-size: 5        # ← HikariCP creates 5 connections
        minimum-idle: 2
```

**In PrimaryDatabaseConfig.java:**
```java
// 1. DataSource reads the config above
@Bean(name = "primaryDataSource")
@ConfigurationProperties(prefix = "spring.datasource.primary")
public DataSource primaryDataSource() {
    return DataSourceBuilder.create().build();
}

// 2. EntityManagerFactory uses DataSource
@Bean(name = "primaryEntityManagerFactory")
public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
        @Qualifier("primaryDataSource") DataSource dataSource) {
    return builder
            .dataSource(dataSource)              // ← Uses DataSource
            .packages("com.example.orders.entity") // ← Scans for entities
            .build();
}

// 3. TransactionManager uses EntityManagerFactory
@Bean(name = "primaryTransactionManager")
public PlatformTransactionManager primaryTransactionManager(
        @Qualifier("primaryEntityManagerFactory") 
        LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
    return new JpaTransactionManager(primaryEntityManagerFactory.getObject());
}
```

---

### 🔄 Deep Dive: How Connection Pooling Works (Lend & Return)

#### The Connection Pool Lifecycle

**Initial State (Application Startup):**
```
HikariCP creates connection pool with 5 connections:

┌─────────────────────────────────────────────┐
│        Connection Pool (MySQL)              │
│                                             │
│  [C1: idle] [C2: idle] [C3: idle]          │
│  [C4: idle] [C5: idle]                     │
│                                             │
│  All connections are:                       │
│  - Real TCP connections to MySQL           │
│  - Authenticated with dev_user/dev_password │
│  - Ready to execute queries                │
└─────────────────────────────────────────────┘
```

#### Step-by-Step: Lending a Connection

**Request 1 arrives:**
```java
// Thread 1 executes this:
orderRepository.findById(1L);
```

**What happens internally:**

```
1. Repository Request
   orderRepository.findById(1L)
         ↓
2. EntityManager needs connection
   entityManager.getConnection()
         ↓
3. DataSource.getConnection() called
   HikariCP checks availability
         ↓
4. HikariCP finds idle connection
   ┌─────────────────────────────┐
   │ Pool Status:                │
   │ [C1: BUSY] ← Lent to Thread1│
   │ [C2: idle]                  │
   │ [C3: idle]                  │
   │ [C4: idle]                  │
   │ [C5: idle]                  │
   └─────────────────────────────┘
         ↓
5. C1 wrapped in ProxyConnection
   (HikariCP intercepts close() calls)
         ↓
6. Connection given to Thread 1
   Thread 1 executes: SELECT * FROM orders WHERE id = 1
```

#### Multiple Concurrent Requests

**5 requests come in simultaneously:**

```
Thread 1: orderRepository.findById(1L)   → Gets C1
Thread 2: orderRepository.findById(2L)   → Gets C2
Thread 3: orderRepository.findById(3L)   → Gets C3
Thread 4: orderRepository.findById(4L)   → Gets C4
Thread 5: orderRepository.findById(5L)   → Gets C5

┌─────────────────────────────┐
│ Pool Status: ALL BUSY       │
│ [C1: Thread1] [C2: Thread2] │
│ [C3: Thread3] [C4: Thread4] │
│ [C5: Thread5]               │
└─────────────────────────────┘
```

**6th request arrives (pool exhausted):**

```
Thread 6: orderRepository.findById(6L)
         ↓
HikariCP: "No idle connections!"
         ↓
Thread 6 WAITS (up to connection-timeout: 30 seconds)
         ↓
┌─────────────────────────────────────────────┐
│ Thread 6 waiting...                         │
│ Timeout counter: 0s...5s...10s...          │
│                                             │
│ Waiting for ANY connection to be returned  │
└─────────────────────────────────────────────┘
```

#### Returning a Connection

**Thread 1 completes its query:**

```java
// Thread 1's code:
Order order = orderRepository.findById(1L).orElseThrow();
// Query complete! ← Returns here
```

**What happens:**

```
1. EntityManager finishes
   resultSet closed
         ↓
2. EntityManager.close() called
   connection.close() invoked
         ↓
3. HikariCP INTERCEPTS close()
   (Connection is NOT actually closed!)
         ↓
4. HikariCP returns C1 to pool
   ┌─────────────────────────────┐
   │ [C1: idle] ← Available!     │
   │ [C2: Thread2] (still busy)  │
   │ [C3: Thread3] (still busy)  │
   │ [C4: Thread4] (still busy)  │
   │ [C5: Thread5] (still busy)  │
   └─────────────────────────────┘
         ↓
5. Thread 6 WAKES UP!
   "Connection available!"
         ↓
6. Thread 6 gets C1
   ┌─────────────────────────────┐
   │ [C1: Thread6] ← Reused!     │
   │ [C2: Thread2]               │
   │ [C3: Thread3]               │
   │ [C4: Thread4]               │
   │ [C5: Thread5]               │
   └─────────────────────────────┘
```

#### The Magic: ProxyConnection

**HikariCP uses Proxy Pattern:**

```java
// What you think happens:
Connection conn = dataSource.getConnection();
conn.close(); // ← You think connection closes

// What actually happens:
Connection conn = dataSource.getConnection();
// ↑ HikariCP returns: ProxyConnection wrapping real connection

conn.close();
// ↑ ProxyConnection intercepts this call
// ↓ Instead of closing, it does:
hikariPool.returnConnection(realConnection);
```

**Proxy Code (Simplified):**

```java
// HikariCP's internal implementation (simplified):
public class ProxyConnection implements Connection {
    private final Connection realConnection;
    private final HikariPool pool;
    private boolean closed = false;
    
    @Override
    public void close() throws SQLException {
        if (closed) return;
        
        // DON'T close the real connection!
        // Instead, return it to pool:
        pool.returnConnection(realConnection);
        closed = true;
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        if (closed) throw new SQLException("Connection closed");
        return realConnection.createStatement(); // Delegate to real
    }
    
    // All other methods delegate to realConnection
}
```

#### Connection States

```
┌─────────────────────────────────────────────┐
│          Connection State Machine           │
├─────────────────────────────────────────────┤
│                                             │
│  [IDLE] ──getConnection()──> [IN_USE]      │
│    ↑                             │          │
│    │                             │          │
│    └────────close()──────────────┘          │
│                                             │
│  [IN_USE] ──timeout──> [REMOVED]           │
│                                             │
│  [IDLE] ──validation_fail──> [REMOVED]     │
│                                             │
│  [REMOVED] ──> [RECREATED] ──> [IDLE]      │
│                                             │
└─────────────────────────────────────────────┘
```

#### Configuration Impact

**Pool Size = 5, Minimum Idle = 2:**

```
Steady State:
┌─────────────────────────────────────┐
│ [C1: idle] [C2: idle]               │  ← Always keep 2 idle
│ [C3: idle] [C4: idle] [C5: idle]    │
└─────────────────────────────────────┘

Under Load:
┌─────────────────────────────────────┐
│ [C1: busy] [C2: busy] [C3: busy]    │
│ [C4: idle] [C5: idle]               │  ← 2 remain idle (if possible)
└─────────────────────────────────────┘

Heavy Load (all taken):
┌─────────────────────────────────────┐
│ [C1: busy] [C2: busy] [C3: busy]    │
│ [C4: busy] [C5: busy]               │
│                                     │
│ New requests WAIT                   │
└─────────────────────────────────────┘
```

#### Connection Timeout Example

**What happens when pool is exhausted:**

```
Time: 0s - Thread 6 requests connection
         ↓
HikariCP: "All 5 connections busy, waiting..."
         ↓
Time: 5s - Still waiting...
         ↓
Time: 10s - Still waiting...
         ↓
Time: 15s - Thread 2 returns C2!
         ↓
Thread 6 gets C2 immediately!
✓ SUCCESS (waited 15s, under 30s timeout)

───────────────────────────────────────

Alternative scenario:
Time: 0s - Thread 6 requests connection
         ↓
Time: 30s - connection-timeout reached
         ↓
❌ SQLException: "Connection is not available, 
   request timed out after 30000ms"
```

#### Real-World Analogy

**Connection Pool = Taxi Stand**

```
🚕 🚕 🚕 🚕 🚕  ← 5 taxis at the stand (idle connections)

Customer 1 arrives: 🚶 → Gets 🚕 (connection lent)
Customer 2 arrives: 🚶 → Gets 🚕 (connection lent)
Customer 3 arrives: 🚶 → Gets 🚕 (connection lent)

Now: 🚕 🚕  ← Only 2 taxis left

Customer 4 arrives: 🚶 → Gets 🚕
Customer 5 arrives: 🚶 → Gets 🚕

Now: (empty stand) ← All taxis busy

Customer 6 arrives: 🚶 → WAITS ⏰
"No taxis available, please wait..."

Taxi 1 returns from trip: 🚕 → Back to stand
Customer 6: "Finally!" → Gets 🚕

Key Point: Taxi is REUSED, not destroyed and recreated!
```

#### Performance Comparison

```
WITHOUT Connection Pool (Creating new each time):
════════════════════════════════════════════════
Request 1: Create (500ms) + Query (50ms) = 550ms
Request 2: Create (500ms) + Query (50ms) = 550ms
Request 3: Create (500ms) + Query (50ms) = 550ms
Total: 1650ms for 3 queries

WITH Connection Pool (Reusing from pool):
════════════════════════════════════════════════
Request 1: Get from pool (5ms) + Query (50ms) = 55ms
Request 2: Get from pool (5ms) + Query (50ms) = 55ms
Request 3: Get from pool (5ms) + Query (50ms) = 55ms
Total: 165ms for 3 queries

RESULT: 10x FASTER! ⚡
```

#### Connection Validation

**HikariCP validates connections automatically:**

```
Scenario: Database server restarts

Before restart:
┌───────────────────────┐
│ [C1: idle - valid]    │
│ [C2: idle - valid]    │
│ [C3: busy]            │
└───────────────────────┘

MySQL server restarts! 💥

After restart:
┌───────────────────────┐
│ [C1: idle - STALE]    │ ← TCP connection broken
│ [C2: idle - STALE]    │ ← TCP connection broken
│ [C3: busy - STALE]    │ ← TCP connection broken
└───────────────────────┘

Next getConnection() call:
1. HikariCP tries to give C1
2. Validates: connection-test-query = "SELECT 1"
3. Query FAILS (connection dead)
4. HikariCP REMOVES C1
5. Creates NEW connection C1'
6. Returns C1' to caller

Result: Application continues working! Self-healing pool.
```

#### Leak Detection (Development Only)

**Configuration:**
```yaml
spring:
  datasource:
    primary:
      hikari:
        leak-detection-threshold: 60000  # 60 seconds
```

**What it does:**

```
Thread 1 gets connection C1
         ↓
60 seconds pass...
         ↓
C1 still not returned!
         ↓
HikariCP logs WARNING:
"Connection leak detection triggered for connection C1
 Stack trace of borrowing thread:"
         ↓
Shows you WHERE in code you forgot to close connection
```

**Example leak:**

```java
// BAD CODE (causes leak):
public void badMethod() {
    Connection conn = dataSource.getConnection();
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM orders");
    // ... process results ...
    // FORGOT TO CLOSE! ← Connection leaked!
}

// GOOD CODE (no leak):
public void goodMethod() {
    try (Connection conn = dataSource.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {
        // ... process results ...
    } // Auto-closes here! ← Connection returned to pool
}
```

#### Summary: Lend & Return Mechanism

```
┌─────────────────────────────────────────────────────────────┐
│                 How HikariCP Works                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. LEND:                                                   │
│     - getConnection() called                                │
│     - HikariCP finds idle connection                        │
│     - Wraps in ProxyConnection                              │
│     - Marks as "in-use"                                     │
│     - Returns to caller                                     │
│                                                             │
│  2. USE:                                                    │
│     - Connection executes queries                           │
│     - Other threads cannot use this connection              │
│     - All methods delegate to real connection               │
│                                                             │
│  3. RETURN:                                                 │
│     - close() called by application                         │
│     - ProxyConnection intercepts                            │
│     - Real connection returned to pool                      │
│     - Marked as "idle"                                      │
│     - Available for next request                            │
│                                                             │
│  Key: Connection is REUSED, not closed!                    │
└─────────────────────────────────────────────────────────────┘
```

---

### Summary

| Component | Purpose | Analogy | Configuration |
|-----------|---------|---------|---------------|
| **DataSource** | Manages connection pool | Parking lot of database connections | HikariCP settings (pool size, timeout) |
| **EntityManagerFactory** | Converts Objects ↔ SQL | Translator between Java and Database | Entity packages, Hibernate dialect |
| **TransactionManager** | Manages transactions | Safety net (all-or-nothing) | Transaction isolation, rollback rules |

**Key Takeaway:**
```
DataSource = "How to connect"          (Connection pooling)
EntityManagerFactory = "What to save"  (Object-Table mapping)
TransactionManager = "When to save"    (Transaction boundaries)
```

All three work together to provide:
✅ **Performance** (connection pooling)  
✅ **Convenience** (automatic SQL generation)  
✅ **Safety** (transaction management)  

---

## 🚀 Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+
- MySQL 8.0+ (primary database)
- PostgreSQL 12+ (secondary database)

### Fastest Way - Interactive Menu
```bash
cd multi-database-connection-pool-template
./quickstart.sh
# Select option 2 for H2 (no database setup needed)
# Or option 3 for MySQL + PostgreSQL with dev profile
```

### Manual Start

```bash
# Build
mvn clean install

# Run with default profile (dev)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=qa
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Build and run JAR
mvn clean package
java -jar target/multi-database-demo-1.0.0.jar --spring.profiles.active=prod
```

### Verify It's Working

```bash
# Health check
curl http://localhost:8080/api/health

# Database statistics
curl http://localhost:8080/api/health/stats

# Create test data
curl -X POST http://localhost:8080/api/health/orders/test
curl -X POST http://localhost:8080/api/health/users/test
```

**Expected Output:**
```
========================================================================
  Multi-Database Application Started Successfully!
========================================================================
  Application:   multi-database-demo-dev
  Profile(s):    [dev]
  Local URL:     http://localhost:8080/
  Primary DB:    MySQL (Orders)
  Secondary DB:  PostgreSQL (Users)
========================================================================
```

---

## 🎯 Environment Profiles

### Profile Overview

| Profile | Database Suffix | Pool Size | Logging | Schema | Use Case |
|---------|----------------|-----------|---------|--------|----------|
| **dev** (default) | `_dev` | 5 | DEBUG (full SQL + params) | auto-update | Local development |
| **qa** | `_qa` | 10 | INFO (moderate SQL) | validate | Testing/QA |
| **prod** | none | 20 | WARN (minimal) | none | Production |

### How to Activate Profiles

#### Using Maven
```bash
# Development (default - no flag needed)
mvn spring-boot:run

# QA
mvn spring-boot:run -Dspring-boot.run.profiles=qa

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Using JAR
```bash
java -jar app.jar --spring.profiles.active=dev
java -jar app.jar --spring.profiles.active=qa
java -jar app.jar --spring.profiles.active=prod
```

#### Using Environment Variable
```bash
export SPRING_PROFILES_ACTIVE=qa
java -jar app.jar
```

#### IntelliJ IDEA
1. Run → Edit Configurations
2. VM Options: `-Dspring.profiles.active=dev`
3. Or Environment: `SPRING_PROFILES_ACTIVE=dev`

### Profile Details

#### 🟢 Development Profile (dev)
**File:** `application-dev.yml`

**Configuration:**
- **Databases:** `ordersdb_dev`, `usersdb_dev` on localhost
- **Credentials:** Plain text in config file (OK for local dev)
- **Pool:** 5 connections max, 2 min idle
- **Logging:** Full SQL with bound parameters (DEBUG)
- **Schema:** Auto-create/update tables (`ddl-auto: update`)
- **Actuator:** All endpoints exposed
- **Features:** Connection leak detection (60s), detailed errors

**When to Use:** Local development, debugging, learning, experimentation

**Example Config:**
```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/ordersdb_dev
      username: dev_user
      password: dev_password
      hikari:
        maximum-pool-size: 5
        leak-detection-threshold: 60000
```

#### 🟡 QA Profile (qa)
**File:** `application-qa.yml`

**Configuration:**
- **Databases:** `ordersdb_qa`, `usersdb_qa` on QA servers
- **Credentials:** Environment variables with fallback
- **Pool:** 10 connections max, 5 min idle
- **Logging:** Moderate SQL logging (INFO), file-based logs
- **Schema:** Validate only, no auto-modification (`ddl-auto: validate`)
- **Actuator:** Limited endpoints (health, info, metrics)
- **Features:** Connection testing, batch operations

**When to Use:** Integration testing, QA validation, pre-production checks

**Example Config:**
```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://qa-server:3306/ordersdb_qa
      username: qa_user
      password: ${DB_PRIMARY_PASSWORD:qa_password}
      hikari:
        maximum-pool-size: 10
        connection-test-query: SELECT 1
```

#### 🔴 Production Profile (prod)
**File:** `application-prod.yml`

**Configuration:**
- **Databases:** `ordersdb`, `usersdb` (from environment variables)
- **Credentials:** Environment variables ONLY (no defaults)
- **Pool:** 20 connections max, 10 min idle
- **Logging:** Minimal (WARN level), production log files
- **Schema:** No modifications allowed (`ddl-auto: none`)
- **Actuator:** Minimal endpoints (health only)
- **Features:** Compression, circuit breakers, production security

**When to Use:** Live production deployment, customer-facing systems

**Example Config:**
```yaml
spring:
  datasource:
    primary:
      jdbc-url: ${DB_PRIMARY_URL}
      username: ${DB_PRIMARY_USERNAME}
      password: ${DB_PRIMARY_PASSWORD}
      hikari:
        maximum-pool-size: 20
```

### Security Best Practices

#### Development ✅
```yaml
username: dev_user
password: dev_password
# Plain text OK for local development
```

#### QA ⚠️
```yaml
username: qa_user
password: ${DB_PRIMARY_PASSWORD:qa_password}
# Environment variable preferred, fallback for convenience
```

#### Production 🔒
```yaml
username: ${DB_PRIMARY_USERNAME}
password: ${DB_PRIMARY_PASSWORD}
# ALWAYS environment variables, NO defaults!
```

**Setting Production Environment Variables:**
```bash
# Linux/Mac
export DB_PRIMARY_USERNAME=prod_user
export DB_PRIMARY_PASSWORD=SuperSecurePassword123!
export DB_SECONDARY_USERNAME=prod_user
export DB_SECONDARY_PASSWORD=AnotherSecurePass456!

# Windows
set DB_PRIMARY_USERNAME=prod_user
set DB_PRIMARY_PASSWORD=SuperSecurePassword123!

# Docker
docker run -e DB_PRIMARY_USERNAME=prod_user \
           -e DB_PRIMARY_PASSWORD=SecurePass \
           my-app:latest
```

---

## 📁 Project Structure

```
multi-database-connection-pool-template/
│
├── src/main/
│   ├── java/com/example/
│   │   ├── MultiDatabaseApplication.java           # Main Spring Boot app
│   │   │
│   │   ├── config/                                 # DATABASE CONFIGURATIONS
│   │   │   ├── PrimaryDatabaseConfig.java          # MySQL (Orders)
│   │   │   └── SecondaryDatabaseConfig.java        # PostgreSQL (Users)
│   │   │
│   │   ├── orders/                                 # PRIMARY DATABASE (MySQL)
│   │   │   ├── entity/Order.java
│   │   │   └── repository/OrderRepository.java
│   │   │
│   │   ├── users/                                  # SECONDARY DATABASE (PostgreSQL)
│   │   │   ├── entity/User.java
│   │   │   └── repository/UserRepository.java
│   │   │
│   │   ├── service/
│   │   │   └── MultiDatabaseService.java           # Cross-database operations
│   │   │
│   │   └── controller/
│   │       └── HealthCheckController.java          # REST endpoints for testing
│   │
│   └── resources/
│       ├── application.yml                          # Common settings (all envs)
│       ├── application-dev.yml                      # Development environment
│       ├── application-qa.yml                       # QA environment
│       ├── application-prod.yml                     # Production environment
│       └── application-example.yml                  # Additional examples
│
├── docs/
│   └── COMPLETE-GUIDE.md                            # Original detailed documentation
│
├── pom.xml                                          # Maven dependencies
├── quickstart.sh                                    # Interactive setup script
├── .gitignore                                       # Git ignore patterns
└── README.md                                        # This file
```

### Key Files Explained

| File | Purpose |
|------|---------|
| `PrimaryDatabaseConfig.java` | MySQL connection config with EntityManager and TransactionManager |
| `SecondaryDatabaseConfig.java` | PostgreSQL connection config with EntityManager and TransactionManager |
| `application.yml` | Common settings, default profile (dev) |
| `application-dev.yml` | Dev environment (localhost, full logging) |
| `application-qa.yml` | QA environment (QA servers, moderate logging) |
| `application-prod.yml` | Prod environment (env vars, minimal logging) |
| `Order.java` | Entity for primary database (MySQL) |
| `User.java` | Entity for secondary database (PostgreSQL) |
| `OrderRepository.java` | JPA repository for Orders |
| `UserRepository.java` | JPA repository for Users |
| `MultiDatabaseService.java` | Example of cross-database operations |
| `HealthCheckController.java` | REST endpoints to test setup |

### Package Organization

**Why separate packages?**
- Each database needs its own package structure
- Configuration classes map specific packages to specific databases
- Clear separation prevents accidental cross-database issues

**Primary Database (MySQL):**
- Entities: `com.example.orders.entity`
- Repositories: `com.example.orders.repository`
- Managed by: `PrimaryDatabaseConfig.java`

**Secondary Database (PostgreSQL):**
- Entities: `com.example.users.entity`
- Repositories: `com.example.users.repository`
- Managed by: `SecondaryDatabaseConfig.java`

---

## ⚙️ Configuration Guide

### Configuration Files

#### application.yml (Common Settings)
Contains settings shared across all environments:
```yaml
spring:
  profiles:
    active: dev  # Default profile
  application:
    name: multi-database-demo
  jpa:
    hibernate:
      ddl-auto: update  # Override per profile
    show-sql: true
```

#### Profile-Specific Files

Each profile has its own YAML file with environment-specific settings:
- `application-dev.yml` - Development
- `application-qa.yml` - QA/Testing
- `application-prod.yml` - Production

### HikariCP Connection Pool Settings

| Setting | Dev | QA | Prod | Description |
|---------|-----|----|----|-------------|
| `maximum-pool-size` | 5 | 10 | 20 | Max number of connections |
| `minimum-idle` | 2 | 5 | 10 | Min idle connections maintained |
| `connection-timeout` | 30s | 30s | 30s | Max wait time for connection |
| `idle-timeout` | 5min | 10min | 10min | Max time connection sits idle |
| `max-lifetime` | 30min | 30min | 30min | Max connection lifetime |
| `leak-detection-threshold` | 60s | none | none | Detect connection leaks |

### Customize Connection Pool

Edit the appropriate `application-{profile}.yml`:

```yaml
spring:
  datasource:
    primary:
      hikari:
        maximum-pool-size: 50        # Increase for high traffic
        minimum-idle: 25
        connection-timeout: 60000    # 60 seconds
        idle-timeout: 300000         # 5 minutes
        max-lifetime: 1800000        # 30 minutes
        leak-detection-threshold: 2000  # 2 seconds
        connection-test-query: SELECT 1
```

### Logging Configuration

```yaml
# Development - Verbose
logging:
  level:
    com.example: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# QA - Moderate
logging:
  level:
    com.example: INFO
    org.hibernate.SQL: DEBUG

# Production - Minimal
logging:
  level:
    com.example: INFO
    org.hibernate.SQL: WARN
```

---

## 💻 Usage Examples

### Using Primary Database (Orders - MySQL)

```java
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    // Create order (saves to MySQL)
    public Order createOrder(Long userId, BigDecimal amount) {
        Order order = new Order();
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setTotalAmount(amount);
        order.setStatus("PENDING");
        return orderRepository.save(order);
    }
    
    // Query orders (from MySQL)
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus("PENDING");
    }
}
```

### Using Secondary Database (Users - PostgreSQL)

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Create user (saves to PostgreSQL)
    public User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        return userRepository.save(user);
    }
    
    // Query users (from PostgreSQL)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
}
```

### Cross-Database Operations

```java
@Service
public class BusinessService {
    
    @Autowired
    private UserRepository userRepository;      // PostgreSQL
    
    @Autowired
    private OrderRepository orderRepository;    // MySQL
    
    // Query data from both databases
    public UserOrderSummary getUserOrderSummary(Long userId) {
        // Get user from PostgreSQL
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Get orders from MySQL
        List<Order> orders = orderRepository.findByUserId(userId);
        
        // Calculate total
        BigDecimal totalSpent = orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new UserOrderSummary(user, orders, totalSpent);
    }
    
    // Create related data in both databases
    @Transactional
    public void createUserWithOrder(UserRequest request) {
        // Create user in PostgreSQL
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        
        // Create order in MySQL
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setTotalAmount(request.getAmount());
        order.setStatus("PENDING");
        orderRepository.save(order);
    }
}
```

### Transaction Management

```java
// Use specific transaction manager
@Service
public class AdvancedService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Transaction on primary database only
    @Transactional(transactionManager = "primaryTransactionManager")
    public void updateOrders() {
        // All operations use MySQL transaction
        orderRepository.findAll().forEach(order -> {
            order.setStatus("PROCESSED");
            orderRepository.save(order);
        });
    }
    
    // Transaction on secondary database only
    @Transactional(transactionManager = "secondaryTransactionManager")
    public void updateUsers() {
        // All operations use PostgreSQL transaction
        userRepository.findAll().forEach(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
    }
}
```

### Testing with REST Endpoints

```bash
# Health check
curl http://localhost:8080/api/health

# Response:
{
  "primaryDatabase": {
    "name": "MySQL (Orders)",
    "status": "UP",
    "recordCount": 10
  },
  "secondaryDatabase": {
    "name": "PostgreSQL (Users)",
    "status": "UP",
    "recordCount": 5
  }
}

# Create test order
curl -X POST http://localhost:8080/api/health/orders/test

# Create test user
curl -X POST http://localhost:8080/api/health/users/test

# Get all orders
curl http://localhost:8080/api/health/orders

# Get all users
curl http://localhost:8080/api/health/users

# Database statistics
curl http://localhost:8080/api/health/stats
```

---

## 🗄️ Database Setup

### Development Environment

```sql
-- ==========================================
-- MySQL (Primary Database)
-- ==========================================
CREATE DATABASE ordersdb_dev 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON ordersdb_dev.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify
USE ordersdb_dev;
SHOW TABLES;

-- ==========================================
-- PostgreSQL (Secondary Database)
-- ==========================================
CREATE DATABASE usersdb_dev;

CREATE USER dev_user WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE usersdb_dev TO dev_user;

-- Verify
\c usersdb_dev
\dt
```

### QA Environment

```sql
-- ==========================================
-- MySQL
-- ==========================================
CREATE DATABASE ordersdb_qa;

CREATE USER 'qa_user'@'%' IDENTIFIED BY 'qa_password';
GRANT ALL PRIVILEGES ON ordersdb_qa.* TO 'qa_user'@'%';
FLUSH PRIVILEGES;

-- ==========================================
-- PostgreSQL
-- ==========================================
CREATE DATABASE usersdb_qa;

CREATE USER qa_user WITH PASSWORD 'qa_password';
GRANT ALL PRIVILEGES ON DATABASE usersdb_qa TO qa_user;
```

### Production Environment

```sql
-- ==========================================
-- MySQL
-- ==========================================
CREATE DATABASE ordersdb;

CREATE USER 'prod_user'@'%' IDENTIFIED BY 'StrongPasswordHere123!';

-- Grant only necessary privileges (not ALL)
GRANT SELECT, INSERT, UPDATE, DELETE ON ordersdb.* TO 'prod_user'@'%';
FLUSH PRIVILEGES;

-- ==========================================
-- PostgreSQL
-- ==========================================
CREATE DATABASE usersdb;

CREATE USER prod_user WITH PASSWORD 'StrongPasswordHere123!';

GRANT CONNECT ON DATABASE usersdb TO prod_user;
\c usersdb
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO prod_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO prod_user;
```

### Database Connection Testing

```bash
# Test MySQL connection
mysql -h localhost -u dev_user -pdev_password ordersdb_dev -e "SELECT 1;"

# Test PostgreSQL connection
psql -h localhost -U dev_user -d usersdb_dev -c "SELECT 1;"

# Test with application
curl http://localhost:8080/api/health
```

---

## 🔧 Integration Options

### Option 1: Use as New Project

```bash
# 1. Copy template
cp -r multi-database-connection-pool-template ~/my-new-project
cd ~/my-new-project

# 2. Update pom.xml
vim pom.xml
# Change: <groupId>, <artifactId>, <name>, <description>

# 3. Configure databases
vim src/main/resources/application-dev.yml
# Update: jdbc-url, username, password

# 4. Create databases
mysql -u root -p
> CREATE DATABASE my_orders_dev;

psql -U postgres
> CREATE DATABASE my_users_dev;

# 5. Build and run
mvn clean install
mvn spring-boot:run

# 6. Verify
curl http://localhost:8080/api/health
```

### Option 2: Integrate into Existing Project

```bash
# 1. Copy configuration classes
cp src/main/java/com/example/config/*.java \
   your-project/src/main/java/your/package/config/

# 2. Update package declarations
# Edit both config files and change:
package com.example.config;  # to your package

# 3. Add dependencies to pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

# 4. Copy profile configurations
cp src/main/resources/application-*.yml \
   your-project/src/main/resources/

# 5. Organize your entities
# Move entities to appropriate packages:
# - com.yourpackage.database1.entity
# - com.yourpackage.database2.entity

# 6. Update config classes
# Update basePackages and packages() to match your structure
```

### Option 3: Use as Reference

Keep this template as:
- Learning resource for multi-database setup
- Reference when configuring new databases
- Documentation for your team
- Starting point for similar projects

---

## 🐛 Troubleshooting

### Profile Not Loading

**Problem:** Application ignores profile or uses wrong one

**Solutions:**
```bash
# 1. Check spelling (case-sensitive!)
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # ✓ Correct
mvn spring-boot:run -Dspring-boot.run.profiles=Dev  # ✗ Wrong

# 2. Verify file exists
ls -la src/main/resources/application-dev.yml

# 3. Check startup logs
# Look for: "The following profiles are active: dev"

# 4. Verify application.yml
spring:
  profiles:
    active: dev  # Default profile
```

### Database Connection Failed

**Problem:** `Could not create connection to database server`

**Solutions:**
```bash
# 1. Verify database is running
# MySQL:
mysql -u root -p -e "SHOW DATABASES;"
# PostgreSQL:
psql -U postgres -l

# 2. Check credentials in profile YAML
vim src/main/resources/application-dev.yml

# 3. Test connection manually
mysql -h localhost -u dev_user -pdev_password ordersdb_dev
psql -h localhost -U dev_user -d usersdb_dev

# 4. Verify database exists
mysql> SHOW DATABASES LIKE 'ordersdb_dev';
psql> \l usersdb_dev

# 5. Check firewall/ports
netstat -an | grep 3306  # MySQL
netstat -an | grep 5432  # PostgreSQL
```

### Connection Pool Exhausted

**Problem:** `HikariPool - Connection is not available, request timed out`

**Solutions:**
```yaml
# 1. Increase pool size
spring:
  datasource:
    primary:
      hikari:
        maximum-pool-size: 20  # Increase
        minimum-idle: 10

# 2. Increase timeout
        connection-timeout: 60000  # 60 seconds

# 3. Check for connection leaks
# Enable in dev:
        leak-detection-threshold: 60000

# 4. Review long-running queries
# Check MySQL:
mysql> SHOW PROCESSLIST;

# 5. Restart application
mvn spring-boot:run
```

### Wrong Database Being Used

**Problem:** Data saved to wrong database

**Solutions:**
```bash
# 1. Check active profile in logs
# Look for: "Profile(s): [dev]"

# 2. Verify package configuration in config class
@EnableJpaRepositories(
    basePackages = "com.example.orders.repository"  # Must match!
)

# 3. Check entity package scanning
.packages("com.example.orders.entity")  # Must match!

# 4. Verify entity is in correct package
# Orders should be in: com.example.orders.entity
# Users should be in: com.example.users.entity

# 5. Check repository package
# OrderRepository in: com.example.orders.repository
# UserRepository in: com.example.users.repository
```

### Schema/Table Errors

**Problem:** Table doesn't exist or schema mismatch

**Solutions:**
```yaml
# For development - Auto-create tables
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # or 'update'

# For production - Manual control
spring:
  jpa:
    hibernate:
      ddl-auto: none

# Enable SQL logging to see what's happening
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Bean Configuration Errors

**Problem:** `No bean named 'entityManagerFactory' available`

**Solutions:**
```java
// 1. Verify all annotations present
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.orders.repository",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)

// 2. Check bean names match everywhere
@Bean(name = "primaryEntityManagerFactory")
@Bean(name = "primaryTransactionManager")
@Bean(name = "primaryDataSource")

// 3. Verify @Qualifier matches
@Qualifier("primaryDataSource")
```

### Environment Variable Not Set

**Problem:** Production profile fails with missing env vars

**Solutions:**
```bash
# 1. Set all required variables
export DB_PRIMARY_USERNAME=prod_user
export DB_PRIMARY_PASSWORD=secure_password
export DB_SECONDARY_USERNAME=prod_user
export DB_SECONDARY_PASSWORD=secure_password

# 2. Verify they're set
echo $DB_PRIMARY_USERNAME
env | grep DB_

# 3. For Docker
docker run -e DB_PRIMARY_USERNAME=user -e DB_PRIMARY_PASSWORD=pass myapp

# 4. For Kubernetes
# Create secret:
kubectl create secret generic db-credentials \
  --from-literal=username=prod_user \
  --from-literal=password=secure_pass
```

---

## 🐳 Docker Support

### Dockerfile

```dockerfile
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy JAR file
COPY target/multi-database-demo-1.0.0.jar app.jar

# Set default profile
ENV SPRING_PROFILES_ACTIVE=prod

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
version: '3.8'

services:
  # MySQL Database
  mysql:
    image: mysql:8.0
    container_name: mysql-orders
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ordersdb_dev
      MYSQL_USER: dev_user
      MYSQL_PASSWORD: dev_password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 3

  # PostgreSQL Database
  postgres:
    image: postgres:14
    container_name: postgres-users
    environment:
      POSTGRES_DB: usersdb_dev
      POSTGRES_USER: dev_user
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U dev_user"]
      interval: 10s
      timeout: 5s
      retries: 3

  # Application
  app:
    build: .
    container_name: multi-db-app
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_PRIMARY_URL: jdbc:mysql://mysql:3306/ordersdb_dev
      DB_PRIMARY_USERNAME: dev_user
      DB_PRIMARY_PASSWORD: dev_password
      DB_SECONDARY_URL: jdbc:postgresql://postgres:5432/usersdb_dev
      DB_SECONDARY_USERNAME: dev_user
      DB_SECONDARY_PASSWORD: dev_password
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 3s
      retries: 3

volumes:
  mysql-data:
  postgres-data:
```

### Build and Run with Docker

```bash
# Build application
mvn clean package

# Build Docker image
docker build -t multi-db-app:1.0.0 .

# Run with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f app

# Check health
curl http://localhost:8080/api/health

# Stop
docker-compose down

# Clean up volumes
docker-compose down -v
```

---

## 🎨 Customization

### Add a Third Database

**1. Create Configuration Class**

```java
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.inventory.repository",
    entityManagerFactoryRef = "tertiaryEntityManagerFactory",
    transactionManagerRef = "tertiaryTransactionManager"
)
public class TertiaryDatabaseConfig {
    
    @Bean(name = "tertiaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.tertiary")
    public DataSource tertiaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "tertiaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tertiaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("tertiaryDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        
        return builder
                .dataSource(dataSource)
                .packages("com.example.inventory.entity")
                .persistenceUnit("tertiary")
                .properties(properties)
                .build();
    }
    
    @Bean(name = "tertiaryTransactionManager")
    public PlatformTransactionManager tertiaryTransactionManager(
            @Qualifier("tertiaryEntityManagerFactory") 
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}
```

**2. Add to application-dev.yml**

```yaml
spring:
  datasource:
    tertiary:
      jdbc-url: jdbc:postgresql://localhost:5432/inventorydb_dev
      username: dev_user
      password: dev_password
      driver-class-name: org.postgresql.Driver
      hikari:
        maximum-pool-size: 5
```

**3. Create Packages**
- `com.example.inventory.entity`
- `com.example.inventory.repository`

### Change Database Type

**Switch from MySQL to PostgreSQL for Primary:**

```yaml
# application-dev.yml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:postgresql://localhost:5432/ordersdb_dev
      driver-class-name: org.postgresql.Driver
```

```java
// PrimaryDatabaseConfig.java
properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
```

**Switch to Oracle:**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
</dependency>
```

```yaml
# application-dev.yml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:oracle:thin:@localhost:1521:ORCL
      driver-class-name: oracle.jdbc.OracleDriver
```

```java
// Config class
properties.put("hibernate.dialect", "org.hibernate.dialect.Oracle12cDialect");
```

### Change Package Names

```bash
# 1. Rename packages in IDE (Refactor → Rename)
com.example → com.yourcompany
orders → orders_module
users → users_module

# 2. Update PrimaryDatabaseConfig.java
@EnableJpaRepositories(
    basePackages = "com.yourcompany.orders_module.repository",
    // ...
)

.packages("com.yourcompany.orders_module.entity")

# 3. Update SecondaryDatabaseConfig.java similarly
```

### Add Custom Repository Methods

```java
// OrderRepository.java
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Custom query methods
    List<Order> findByUserId(Long userId);
    
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    List<Order> findOrdersBetweenDates(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount > :amount")
    List<Order> findHighValueOrders(@Param("amount") BigDecimal amount);
}
```

---

## ⚡ Quick Reference

### Common Commands

```bash
# Build
mvn clean install

# Run profiles
mvn spring-boot:run                                    # dev (default)
mvn spring-boot:run -Dspring-boot.run.profiles=qa     # qa
mvn spring-boot:run -Dspring-boot.run.profiles=prod   # prod

# Build JAR
mvn clean package

# Run JAR
java -jar target/multi-database-demo-1.0.0.jar --spring.profiles.active=prod

# Tests
mvn test

# Interactive
./quickstart.sh

# Health checks
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/stats
curl http://localhost:8080/actuator/health
```

### URLs

```
Application:     http://localhost:8080
Health Check:    http://localhost:8080/api/health
Database Stats:  http://localhost:8080/api/health/stats
Orders:          http://localhost:8080/api/health/orders
Users:           http://localhost:8080/api/health/users
Actuator:        http://localhost:8080/actuator
H2 Console:      http://localhost:8080/h2-console (H2 profile only)
```

### Configuration Files

```
Common:          src/main/resources/application.yml
Development:     src/main/resources/application-dev.yml
QA:              src/main/resources/application-qa.yml
Production:      src/main/resources/application-prod.yml
Examples:        src/main/resources/application-example.yml
```

### Key Java Classes

```
Main:            src/main/java/com/example/MultiDatabaseApplication.java
Primary Config:  src/main/java/com/example/config/PrimaryDatabaseConfig.java
Secondary Config: src/main/java/com/example/config/SecondaryDatabaseConfig.java
Order Entity:    src/main/java/com/example/orders/entity/Order.java
User Entity:     src/main/java/com/example/users/entity/User.java
Order Repo:      src/main/java/com/example/orders/repository/OrderRepository.java
User Repo:       src/main/java/com/example/users/repository/UserRepository.java
Service:         src/main/java/com/example/service/MultiDatabaseService.java
Controller:      src/main/java/com/example/controller/HealthCheckController.java
```

### Environment Variables (Production)

```bash
# Required for prod profile
export DB_PRIMARY_URL=jdbc:mysql://prod-server:3306/ordersdb
export DB_PRIMARY_USERNAME=prod_user
export DB_PRIMARY_PASSWORD=secure_password
export DB_SECONDARY_URL=jdbc:postgresql://prod-server:5432/usersdb
export DB_SECONDARY_USERNAME=prod_user
export DB_SECONDARY_PASSWORD=secure_password
```

### Pool Size Recommendations

| Traffic Level | Connections | Profile |
|--------------|-------------|---------|
| Low (dev/testing) | 5-10 | dev, qa |
| Medium | 10-20 | qa, small prod |
| High | 20-50 | production |
| Very High | 50-100 | large production |

### Database Naming Convention

| Environment | Primary DB | Secondary DB |
|-------------|-----------|--------------|
| Development | `ordersdb_dev` | `usersdb_dev` |
| QA | `ordersdb_qa` | `usersdb_qa` |
| Production | `ordersdb` | `usersdb` |

---

## ✅ Setup Checklist

### New Project Setup
- [ ] Copy template to project location
- [ ] Update `pom.xml` (groupId, artifactId, name)
- [ ] Create development databases
- [ ] Configure `application-dev.yml` with database credentials
- [ ] Build project: `mvn clean install`
- [ ] Run application: `mvn spring-boot:run`
- [ ] Test health check: `curl http://localhost:8080/api/health`
- [ ] Create test data with POST endpoints
- [ ] Verify data in both databases
- [ ] Configure QA profile
- [ ] Configure production profile with env vars
- [ ] Test with different profiles

### Integration Setup
- [ ] Copy config classes to your project
- [ ] Update package declarations
- [ ] Add dependencies to `pom.xml`
- [ ] Copy profile YAML files
- [ ] Organize entities into separate packages
- [ ] Update basePackages in config classes
- [ ] Update entity scanning packages
- [ ] Test with each profile

---

## 📚 Additional Resources

- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Multiple Datasources](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-access.configure-custom-datasource)
- [Complete Guide](docs/COMPLETE-GUIDE.md) - Original detailed documentation

---

## 🤝 Contributing

This is a template project. Feel free to:
- Customize for your specific needs
- Add more database configurations
- Extend with additional features
- Share with your team or community

---

## 📄 License

This template is provided as-is for educational and commercial use.

---

## 💡 Tips & Best Practices

1. **Always use environment profiles** - Never mix dev and prod settings
2. **Environment variables for prod** - Never hardcode production credentials
3. **Test with H2 first** - Fastest way to verify configuration
4. **Monitor connection pools** - Use actuator metrics in production
5. **Use appropriate pool sizes** - Don't over-provision
6. **Separate packages strictly** - Prevents cross-database issues
7. **Transaction management** - Be explicit when using multiple databases
8. **Log SQL in dev only** - Performance impact in production
9. **Version control** - Add `application-local.yml` to `.gitignore`
10. **Documentation** - Keep this README updated with your changes

---

**🎉 Your multi-database template is ready to use!**

**Last Updated:** March 17, 2026  
**Spring Boot:** 2.7.14  
**Java:** 11+  
**Author:** Multi-Database Template Team

---

**Need Help?** Check the troubleshooting section or review `docs/COMPLETE-GUIDE.md` for detailed information.

