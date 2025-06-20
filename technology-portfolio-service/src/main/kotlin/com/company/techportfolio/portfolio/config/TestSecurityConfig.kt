package com.company.techportfolio.portfolio.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * Test Security Configuration for Technology Portfolio Service
 * 
 * This configuration class provides a simplified security setup for testing
 * environments. It disables most security restrictions to allow tests to focus
 * on business logic rather than security concerns.
 * 
 * Key features:
 * - Disables authentication for all endpoints
 * - Permits all requests without security checks
 * - Disables CSRF protection
 * - Disables CORS restrictions
 * - Simplified filter chain for testing
 * 
 * Profile activation:
 * - Only active when 'test' profile is enabled
 * - Provides alternative to production security for testing
 * - Allows tests to run without authentication setup
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Configuration
@EnableWebFluxSecurity
@Profile("test")
class TestSecurityConfig {

    /**
     * Configures a simplified security filter chain for testing.
     * 
     * Creates a security configuration that permits all requests without
     * authentication or authorization checks. This allows tests to focus
     * on business logic validation rather than security setup.
     * 
     * Security settings:
     * - All endpoints are publicly accessible
     * - No authentication required
     * - No authorization checks
     * - CSRF protection disabled
     * - CORS restrictions disabled
     * 
     * @param http ServerHttpSecurity configuration builder
     * @return SecurityWebFilterChain with permissive security configuration
     */
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { exchanges ->
                exchanges.anyExchange().permitAll()
            }
            .csrf { csrf -> csrf.disable() }
            .cors { cors -> cors.disable() }
            .build()
    }
} 