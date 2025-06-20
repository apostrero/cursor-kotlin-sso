package com.company.techportfolio.portfolio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

/**
 * Technology Portfolio Service Application
 * 
 * This is the main entry point for the Technology Portfolio Service, a microservice
 * that manages technology portfolios and their associated technologies within an
 * enterprise architecture. The service follows hexagonal architecture principles
 * and provides RESTful APIs for portfolio management operations.
 * 
 * ## Key Features:
 * - Portfolio creation, update, and management
 * - Technology inventory and lifecycle management
 * - Cost tracking and assessment capabilities
 * - Multi-tenant organization support
 * - Event-driven architecture integration
 * - Role-based access control
 * 
 * ## Architecture:
 * - **Domain Layer**: Core business logic and entities
 * - **Application Layer**: Use cases and service orchestration
 * - **Adapter Layer**: Infrastructure adapters (REST, JPA, Events)
 * - **Configuration**: Spring Boot auto-configuration and custom configs
 * 
 * ## Integration:
 * - Service discovery via Spring Cloud
 * - Event publishing for domain events
 * - Database persistence with JPA/Hibernate
 * - JWT-based authentication and authorization
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioService
 * @see PortfolioController
 */
@SpringBootApplication
@EnableDiscoveryClient
class TechnologyPortfolioServiceApplication

/**
 * Main entry point for the Technology Portfolio Service application.
 * 
 * Initializes the Spring Boot application context and starts the embedded
 * web server. The application will register itself with the service discovery
 * mechanism and be available for client requests.
 * 
 * @param args Command line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<TechnologyPortfolioServiceApplication>(*args)
} 