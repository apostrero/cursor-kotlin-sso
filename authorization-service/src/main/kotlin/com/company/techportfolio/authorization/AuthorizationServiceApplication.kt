package com.company.techportfolio.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

/**
 * Main application class for the Authorization Service microservice.
 * 
 * This Spring Boot application provides authorization and permission management
 * capabilities for the Technology Portfolio system. It handles user role-based
 * access control (RBAC), permission verification, and authorization decisions
 * for various system resources.
 * 
 * Key features:
 * - User role and permission management
 * - Authorization decision making
 * - RESTful API endpoints for authorization queries
 * - Service discovery integration via Eureka
 * - Database persistence for user roles and permissions
 * 
 * The service integrates with:
 * - API Gateway for authorization requests
 * - User Management Service for user data
 * - Audit Service for authorization logging
 * 
 * Configuration:
 * - Eureka service discovery enabled
 * - JPA/Hibernate for database operations
 * - Flyway for database migrations
 * - Spring Security for endpoint protection
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
class AuthorizationServiceApplication

/**
 * Main entry point for the Authorization Service application.
 * 
 * Bootstraps the Spring Boot application context and starts the embedded
 * web server. The application will register itself with the Eureka service
 * registry and begin accepting authorization requests.
 * 
 * @param args Command line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<AuthorizationServiceApplication>(*args)
} 