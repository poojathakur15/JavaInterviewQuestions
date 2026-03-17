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

/**
 * Secondary Database Configuration (PostgreSQL - Users Database)
 * 
 * This configuration class sets up the SECONDARY database connection with:
 * - DataSource: Connection pool to PostgreSQL database
 * - EntityManagerFactory: JPA entity management for User entities
 * - TransactionManager: Handles transactions for User operations
 * 
 * Key Points:
 * 1. NO @Primary annotation (only one datasource can be primary)
 * 2. basePackages in @EnableJpaRepositories maps repositories to this DB
 * 3. All repositories in "com.example.users.repository" will use this database
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.users.repository",  // Package for User repositories
    entityManagerFactoryRef = "secondaryEntityManagerFactory",
    transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryDatabaseConfig {

    /**
     * Secondary DataSource Bean
     * 
     * @ConfigurationProperties reads from application.yml:
     *   spring.datasource.secondary.jdbc-url
     *   spring.datasource.secondary.username
     *   spring.datasource.secondary.password
     *   spring.datasource.secondary.driver-class-name
     * 
     * Note: NO @Primary annotation - this is the secondary database
     */
    @Bean(name = "secondaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Secondary EntityManagerFactory Bean
     * 
     * This bean manages JPA entities from "com.example.users.entity" package
     * It connects these entities to the secondaryDataSource
     * 
     * @param builder - Auto-configured by Spring Boot
     * @param dataSource - The secondaryDataSource bean
     * @return LocalContainerEntityManagerFactoryBean for secondary database
     */
    @Bean(name = "secondaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("secondaryDataSource") DataSource dataSource) {
        
        // Hibernate properties specific to PostgreSQL
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        
        return builder
                .dataSource(dataSource)
                .packages("com.example.users.entity")  // Package for User entities
                .persistenceUnit("secondary")           // Logical name for this persistence unit
                .properties(properties)
                .build();
    }

    /**
     * Secondary TransactionManager Bean
     * 
     * Manages transactions for all operations using secondaryEntityManagerFactory
     * Use @Transactional(transactionManager = "secondaryTransactionManager") 
     * to explicitly use this transaction manager
     * 
     * @param secondaryEntityManagerFactory - The secondary entity manager factory
     * @return JpaTransactionManager for secondary database
     */
    @Bean(name = "secondaryTransactionManager")
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory) {
        return new JpaTransactionManager(secondaryEntityManagerFactory.getObject());
    }
}

