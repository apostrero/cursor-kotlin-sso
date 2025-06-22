package com.company.techportfolio.gateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

/**
 * Application configuration class for the API Gateway.
 *
 * This configuration class defines common beans and application-wide settings
 * for the API Gateway service. It provides essential infrastructure components
 * like HTTP clients and other shared resources needed across the application.
 *
 * Features:
 * - HTTP client configuration with configurable timeouts
 * - Error handling configuration
 * - Connection pooling settings
 * - Request/response logging configuration
 *
 * Configuration properties:
 * - http.client.connect-timeout: Connection timeout in seconds (default: 5)
 * - http.client.read-timeout: Read timeout in seconds (default: 10)
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Configuration
class ApplicationConfig {

    @Value("\${http.client.connect-timeout:5}")
    private var connectTimeoutSeconds: Int = 5

    @Value("\${http.client.read-timeout:10}")
    private var readTimeoutSeconds: Int = 10

    /**
     * Creates a configured RestTemplate bean for HTTP client operations.
     *
     * This RestTemplate is configured with appropriate timeouts and settings
     * for making HTTP requests to downstream microservices and external systems.
     * Used by various adapters for service-to-service communication.
     *
     * Configuration includes:
     * - Configurable connection timeout (default: 5 seconds)
     * - Configurable read timeout (default: 10 seconds)
     * - Default error handling
     * - Standard HTTP client behavior
     *
     * @param builder RestTemplateBuilder provided by Spring Boot for configuration
     * @return Configured RestTemplate instance with connection and read timeouts
     */
    @Bean
    @Suppress("DEPRECATION")
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds.toLong()))
            .setReadTimeout(Duration.ofSeconds(readTimeoutSeconds.toLong()))
            .build()
    }

    /**
     * Creates a RestTemplateBuilder bean for custom HTTP client configuration.
     *
     * This builder can be used by other components to create custom RestTemplate
     * instances with specific configurations for different downstream services.
     *
     * @return RestTemplateBuilder instance for custom configurations
     */
    @Bean
    fun restTemplateBuilder(): RestTemplateBuilder {
        return RestTemplateBuilder()
    }
} 