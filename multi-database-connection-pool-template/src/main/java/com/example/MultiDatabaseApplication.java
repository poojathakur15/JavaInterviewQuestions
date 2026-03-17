package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Multi-Database Connection Pool Application
 * 
 * This is the main entry point for the Spring Boot application.
 * It demonstrates how to configure multiple databases with connection pooling.
 * 
 * Features:
 * - Multiple database connections (MySQL and PostgreSQL)
 * - HikariCP connection pooling
 * - Separate entity managers for each database
 * - Transaction management per database
 * - Environment-specific profiles (dev, qa, prod)
 * 
 * To run with specific profile:
 * - Dev: mvn spring-boot:run -Dspring-boot.run.profiles=dev
 * - QA:  mvn spring-boot:run -Dspring-boot.run.profiles=qa
 * - Prod: java -jar app.jar --spring.profiles.active=prod
 * 
 * @author Multi-Database Template
 * @version 1.0.0
 */
@SpringBootApplication
public class MultiDatabaseApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MultiDatabaseApplication.class, args);
        Environment env = context.getEnvironment();
        
        logApplicationStartup(env);
    }
    
    /**
     * Logs application startup information including active profile
     */
    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        String hostAddress = "localhost";
        
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // Use localhost as fallback
        }
        
        String[] activeProfiles = env.getActiveProfiles();
        String profileInfo = activeProfiles.length > 0 
            ? Arrays.toString(activeProfiles) 
            : "[default]";
        
        System.out.println("\n" +
            "========================================================================\n" +
            "  Multi-Database Application Started Successfully!\n" +
            "========================================================================\n" +
            "  Application:   " + env.getProperty("spring.application.name") + "\n" +
            "  Profile(s):    " + profileInfo + "\n" +
            "  Local URL:     " + protocol + "://localhost:" + serverPort + contextPath + "\n" +
            "  External URL:  " + protocol + "://" + hostAddress + ":" + serverPort + contextPath + "\n" +
            "------------------------------------------------------------------------\n" +
            "  Primary DB:    MySQL (Orders)\n" +
            "  Secondary DB:  PostgreSQL (Users)\n" +
            "------------------------------------------------------------------------\n" +
            "  Health Check:  " + protocol + "://localhost:" + serverPort + "/api/health\n" +
            "  Actuator:      " + protocol + "://localhost:" + serverPort + "/actuator\n" +
            "========================================================================\n");
    }
}

