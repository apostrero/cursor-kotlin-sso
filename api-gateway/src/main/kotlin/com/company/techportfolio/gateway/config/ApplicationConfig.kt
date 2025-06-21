package com.company.techportfolio.gateway.config

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
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Configuration
class ApplicationConfig {

    /**
     * Creates a configured RestTemplate bean for HTTP client operations.
     *
     * This RestTemplate is configured with appropriate timeouts and settings
     * for making HTTP requests to downstream microservices and external systems.
     * Used by various adapters for service-to-service communication.
     *
     * @param builder RestTemplateBuilder provided by Spring Boot for configuration
     * @return Configured RestTemplate instance with connection and read timeouts
     */
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build()
    }
} 