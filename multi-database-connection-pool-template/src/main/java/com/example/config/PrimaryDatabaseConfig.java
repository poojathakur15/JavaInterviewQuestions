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

/**
 * Primary Database Configuration (MySQL - Orders Database)
 * 
 * This configuration class sets up the PRIMARY database connection with:
 * - DataSource: Connection pool to MySQL database
 * - EntityManagerFactory: JPA entity management for Order entities
 * - TransactionManager: Handles transactions for Order operations
 * 
 * Key Points:
 * 1. @Primary annotation makes this the default database
 * 2. basePackages in @EnableJpaRepositories maps repositories to this DB
 * 3. All repositories in "com.example.orders.repository" will use this database
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.orders.repository",  // Package for Order repositories
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDatabaseConfig {

    /**
     * Primary DataSource Bean (HikariCP Connection Pool)
     * 
     * HOW CONNECTION POOL WORKS:
     * ═══════════════════════════════════════════════════════════════════
     * 
     * 1. INITIALIZATION (Application Startup):
     *    - HikariCP creates a pool of database connections (e.g., 5 connections)
     *    - Each connection is a real TCP connection to MySQL
     *    - Connections are validated and kept alive (idle)
     * 
     * 2. LENDING OUT (When query is executed):
     *    - Application calls: orderRepository.findById(1L)
     *    - EntityManager requests: dataSource.getConnection()
     *    - HikariCP checks:
     *      a) Is there an available (idle) connection? → Return it immediately
     *      b) All busy? → Wait up to connection-timeout (30s)
     *      c) Still none? → Throw SQLException: "Connection is not available"
     * 
     * 3. USAGE (During query execution):
     *    - Connection is marked as "in-use" (borrowed)
     *    - Application executes: SELECT * FROM orders WHERE id = 1
     *    - Connection is NOT available to other threads
     * 
     * 4. RETURNING (After query completes):
     *    - Application calls: connection.close()
     *    - BUT connection is NOT actually closed!
     *    - HikariCP intercepts the close() call
     *    - Connection is returned to pool and marked as "available"
     *    - Same connection can be reused by next request
     * 
     * 5. VALIDATION & RECYCLING:
     *    - Idle connections tested periodically
     *    - Stale connections recycled after max-lifetime (30min)
     *    - Failed connections removed and replaced
     * 
     * CONFIGURATION FROM application.yml:
     * ─────────────────────────────────────────────────────────────────
     * spring.datasource.primary:
     *   jdbc-url: jdbc:mysql://localhost:3306/ordersdb_dev
     *   username: dev_user
     *   password: dev_password
     *   hikari:
     *     maximum-pool-size: 5        # Total connections in pool
     *     minimum-idle: 2             # Always keep 2 ready
     *     connection-timeout: 30000   # Wait 30s for connection
     *     idle-timeout: 600000        # Recycle idle after 10min
     *     max-lifetime: 1800000       # Force recycle after 30min
     * 
     * WHY CONNECTION POOLING?
     * ─────────────────────────────────────────────────────────────────
     * WITHOUT Pool: Create new connection (500ms) + Execute query (50ms) = 550ms
     * WITH Pool:    Get from pool (5ms) + Execute query (50ms) = 55ms
     * 
     * 10x FASTER! Plus database servers have connection limits.
     * 
     * @Primary annotation makes this the default when @Autowired DataSource is used
     */
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        // DataSourceBuilder creates HikariDataSource (HikariCP is default in Spring Boot)
        // HikariCP wraps each JDBC Connection with a ProxyConnection
        // ProxyConnection intercepts close() calls to return connection to pool instead
        return DataSourceBuilder.create().build();
    }

    /**
     * Primary EntityManagerFactory Bean
     * 
     * This bean manages JPA entities from "com.example.orders.entity" package
     * It connects these entities to the primaryDataSource
     * 
     * @param builder - Auto-configured by Spring Boot
     * @param dataSource - The primaryDataSource bean
     * @return LocalContainerEntityManagerFactoryBean for primary database
     */
    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource dataSource) {
        
        // Hibernate properties specific to MySQL
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        
        return builder
                .dataSource(dataSource)
                .packages("com.example.orders.entity")  // Package for Order entities
                .persistenceUnit("primary")              // Logical name for this persistence unit
                .properties(properties)
                .build();
    }

    /**
     * Primary TransactionManager Bean
     * 
     * Manages transactions for all operations using primaryEntityManagerFactory
     * All @Transactional annotations without explicit transactionManager will use this
     * 
     * @param primaryEntityManagerFactory - The primary entity manager factory
     * @return JpaTransactionManager for primary database
     */
    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
        return new JpaTransactionManager(primaryEntityManagerFactory.getObject());
    }
}

