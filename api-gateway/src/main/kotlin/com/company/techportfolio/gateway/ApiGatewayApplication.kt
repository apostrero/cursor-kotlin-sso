package com.company.techportfolio.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

/**
 * Main application class for the Technology Portfolio API Gateway service.
 * 
 * This Spring Boot application serves as the central entry point for the microservices architecture,
 * providing authentication, authorization, and request routing capabilities. The gateway handles
 * both SAML-based SSO authentication and mock authentication for development purposes.
 * 
 * Key responsibilities:
 * - SAML 2.0 SSO authentication integration
 * - JWT token generation and validation
 * - Request routing to downstream microservices
 * - Centralized security enforcement
 * - Service discovery integration via Eureka
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
class ApiGatewayApplication

/**
 * Application entry point that starts the Spring Boot API Gateway service.
 * 
 * Initializes the Spring application context with all necessary configurations
 * including security, service discovery, and routing capabilities.
 * 
 * @param args Command line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
} 