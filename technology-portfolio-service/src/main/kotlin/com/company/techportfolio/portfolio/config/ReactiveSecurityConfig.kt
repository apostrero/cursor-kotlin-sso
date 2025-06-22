package com.company.techportfolio.portfolio.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Flux

/**
 * Reactive Security Configuration for Technology Portfolio Service
 *
 * This configuration class provides WebFlux-based security configuration for the
 * technology portfolio service, migrating from Spring Web MVC security to reactive
 * security patterns. It configures JWT-based authentication and authorization
 * using reactive security components.
 *
 * Key features:
 * - JWT resource server configuration for token validation
 * - Role-based access control (RBAC) with reactive patterns
 * - CSRF protection disabled for API endpoints
 * - CORS configuration for cross-origin requests
 * - Public endpoints for health checks and actuator
 * - Reactive security filter chain
 * - JWT authentication converter with custom authorities mapping
 *
 * Security endpoints:
 * - Public: /actuator/health, /actuator/info
 * - Protected: All /api/## endpoints require authentication
 * - Admin only: /api/v1/portfolios/stream endpoints
 *
 * JWT Configuration:
 * - Validates JWT tokens from API Gateway
 * - Extracts authorities from JWT claims
 * - Supports custom role mapping
 * - Handles token expiration and validation
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Configuration
@EnableWebFluxSecurity
class ReactiveSecurityConfig {

    /**
     * Configures the reactive security filter chain for WebFlux.
     *
     * Sets up the complete security configuration including:
     * - JWT resource server configuration
     * - URL-based authorization rules
     * - CSRF protection settings
     * - CORS configuration
     * - Public endpoint access
     * - Custom JWT authentication converter
     *
     * Authorization rules:
     * - Public endpoints: actuator health and info
     * - All /api/## endpoints require authentication
     * - Stream endpoints require ADMIN role
     * - Other endpoints require USER role
     *
     * @param http ServerHttpSecurity configuration builder
     * @return SecurityWebFilterChain with complete reactive security configuration
     */
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                    .pathMatchers("/api/v1/portfolios/stream/**").hasRole("ADMIN")
                    .pathMatchers("/api/**").hasRole("USER")
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(reactiveJwtAuthenticationConverter())
                }
            }
            .csrf { csrf -> csrf.disable() }
            .cors { cors -> cors.disable() }
            .build()
    }

    /**
     * Configures reactive JWT authentication converter with custom authorities mapping.
     *
     * Creates a reactive JWT authentication converter that extracts authorities from
     * JWT claims and maps them to Spring Security authorities. This enables
     * role-based access control based on JWT token contents.
     *
     * Authority mapping:
     * - Extracts "authorities" claim from JWT
     * - Maps each authority to a GrantedAuthority
     * - Supports both "ROLE_" prefixed and custom authorities
     * - Handles empty or missing authorities gracefully
     *
     * @return ReactiveJwtAuthenticationConverter configured for custom authority extraction
     */
    @Bean
    fun reactiveJwtAuthenticationConverter(): ReactiveJwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities")
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_")

        val jwtAuthenticationConverter = ReactiveJwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = grantedAuthoritiesConverter.convert(jwt)
            Flux.fromIterable(authorities ?: emptyList())
        }

        return jwtAuthenticationConverter
    }

    /**
     * Configures CSRF token filter for reactive applications.
     *
     * Creates a WebFilter that adds CSRF token information to the response
     * headers for client-side applications that need CSRF protection.
     * This is useful for applications that use both API and form-based interactions.
     *
     * @return WebFilter that adds CSRF token to response headers
     */
    @Bean
    fun csrfTokenFilter(): WebFilter {
        return WebFilter { exchange, chain ->
            exchange.getAttribute<CsrfToken>(CsrfToken::class.java.name)?.let { token ->
                exchange.response.headers.add("X-CSRF-TOKEN", token.token)
            }
            chain.filter(exchange)
        }
    }
} 