#!/bin/bash

# Multi-Database Connection Pool Template - Quick Start Script
# This script helps you quickly set up and test the template

echo "=========================================="
echo "Multi-Database Connection Pool Template"
echo "Quick Start Script"
echo "=========================================="
echo ""

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "Checking prerequisites..."
echo ""

# Check Java
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    echo "✓ Java is installed (version check: $JAVA_VERSION)"
else
    echo "✗ Java is not installed. Please install Java 11 or higher."
    exit 1
fi

# Check Maven
if command_exists mvn; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✓ Maven is installed ($MVN_VERSION)"
else
    echo "✗ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo ""
echo "=========================================="
echo "Setup Options"
echo "=========================================="
echo "1. Build the project"
echo "2. Run with H2 databases (no external DB needed)"
echo "3. Run with MySQL + PostgreSQL (DEV profile)"
echo "4. Run with MySQL + PostgreSQL (QA profile)"
echo "5. Run with MySQL + PostgreSQL (PROD profile)"
echo "6. Run tests"
echo "7. Check database connections"
echo "8. Clean and rebuild"
echo "9. Generate project documentation"
echo "0. Exit"
echo ""

read -p "Select an option (0-9): " option

case $option in
    1)
        echo ""
        echo "Building the project..."
        mvn clean install
        echo ""
        echo "Build complete!"
        ;;

    2)
        echo ""
        echo "Running with H2 in-memory databases..."
        echo "No external database setup required!"
        echo ""

        # Create temporary H2 configuration
        cat > src/main/resources/application-h2.yml << 'EOF'
spring:
  datasource:
    primary:
      jdbc-url: jdbc:h2:mem:ordersdb;DB_CLOSE_DELAY=-1;MODE=MySQL
      username: sa
      password:
      driver-class-name: org.h2.Driver
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
        pool-name: PrimaryH2Pool

    secondary:
      jdbc-url: jdbc:h2:mem:usersdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
      username: sa
      password:
      driver-class-name: org.h2.Driver
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
        pool-name: SecondaryH2Pool

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

server:
  port: 8080

logging:
  level:
    com.example: DEBUG
EOF

        echo "Starting application with H2 databases..."
        echo ""
        echo "Access points:"
        echo "  Application: http://localhost:8080"
        echo "  Health Check: http://localhost:8080/api/health"
        echo "  H2 Console: http://localhost:8080/h2-console"
        echo ""
        mvn spring-boot:run -Dspring-boot.run.profiles=h2
        ;;

    3)
        echo ""
        echo "Running with MySQL + PostgreSQL (DEV profile)..."
        echo ""
        echo "Make sure you have:"
        echo "  - MySQL running on localhost:3306"
        echo "  - PostgreSQL running on localhost:5432"
        echo "  - Created databases: ordersdb_dev, usersdb_dev"
        echo ""
        read -p "Have you set up the databases? (y/n): " confirm

        if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
            echo ""
            echo "Starting application with DEV profile..."
            mvn spring-boot:run -Dspring-boot.run.profiles=dev
        else
            echo ""
            echo "Please set up the databases first."
            echo "Run the following commands:"
            echo ""
            echo "MySQL:"
            echo "  mysql -u root -p"
            echo "  CREATE DATABASE ordersdb_dev;"
            echo ""
            echo "PostgreSQL:"
            echo "  psql -U postgres"
            echo "  CREATE DATABASE usersdb_dev;"
        fi
        ;;

    4)
        echo ""
        echo "Running with MySQL + PostgreSQL (QA profile)..."
        echo ""
        echo "Make sure you have:"
        echo "  - QA MySQL server accessible"
        echo "  - QA PostgreSQL server accessible"
        echo "  - Created databases: ordersdb_qa, usersdb_qa"
        echo ""
        read -p "Continue with QA profile? (y/n): " confirm

        if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
            echo ""
            echo "Starting application with QA profile..."
            mvn spring-boot:run -Dspring-boot.run.profiles=qa
        else
            echo "QA profile startup cancelled."
        fi
        ;;

    5)
        echo ""
        echo "Running with MySQL + PostgreSQL (PROD profile)..."
        echo ""
        echo "⚠️  WARNING: You are about to start in PRODUCTION mode!"
        echo ""
        echo "Make sure you have set these environment variables:"
        echo "  - DB_PRIMARY_USERNAME"
        echo "  - DB_PRIMARY_PASSWORD"
        echo "  - DB_SECONDARY_USERNAME"
        echo "  - DB_SECONDARY_PASSWORD"
        echo ""
        read -p "Are environment variables set? (y/n): " confirm

        if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
            echo ""
            echo "Starting application with PROD profile..."
            mvn spring-boot:run -Dspring-boot.run.profiles=prod
        else
            echo ""
            echo "Please set environment variables first:"
            echo "  export DB_PRIMARY_USERNAME=your_username"
            echo "  export DB_PRIMARY_PASSWORD=your_password"
            echo "  export DB_SECONDARY_USERNAME=your_username"
            echo "  export DB_SECONDARY_PASSWORD=your_password"
        fi
        ;;

    6)
        echo ""
        echo "Running tests..."
        mvn test
        ;;

    7)
        echo ""
        echo "Checking database connections..."
        echo ""

        # Check if application is running
        if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
            echo "Application is running!"
            echo ""
            echo "Health Check Response:"
            curl -s http://localhost:8080/api/health | python -m json.tool 2>/dev/null || curl -s http://localhost:8080/api/health
        else
            echo "Application is not running."
            echo "Start the application first (option 2, 3, 4, or 5)."
        fi
        ;;

    8)
        echo ""
        echo "Cleaning and rebuilding..."
        mvn clean
        mvn install
        echo ""
        echo "Clean build complete!"
        ;;

    9)
        echo ""
        echo "Generating project documentation..."
        mvn javadoc:javadoc
        echo ""
        echo "Documentation generated in: target/site/apidocs/"
        ;;

    0)
        echo ""
        echo "Goodbye!"
        exit 0
        ;;

    *)
        echo ""
        echo "Invalid option. Please select 0-9."
        ;;
esac

echo ""
echo "=========================================="
echo "Useful Commands:"
echo "=========================================="
echo "Build:           mvn clean install"
echo "Run (Dev):       mvn spring-boot:run"
echo "Run (QA):        mvn spring-boot:run -Dspring-boot.run.profiles=qa"
echo "Run (Prod):      mvn spring-boot:run -Dspring-boot.run.profiles=prod"
echo "Test:            mvn test"
echo "Health Check:    curl http://localhost:8080/api/health"
echo "View Profiles:   cat PROFILES.md"
echo "View README:     cat README.md"
echo ""

