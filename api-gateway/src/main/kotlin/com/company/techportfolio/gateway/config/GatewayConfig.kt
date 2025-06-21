package com.company.techportfolio.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!mock-auth")  // Only load when NOT in mock-auth mode
@ConditionalOnProperty(name = ["spring.cloud.gateway.enabled"], havingValue = "true", matchIfMissing = true)
class GatewayConfig {

    /**
     * Configures custom routing rules for the API Gateway.
     *
     * Defines routing rules that map incoming HTTP requests to appropriate
     * backend microservices. Each route includes path matching, filters for
     * request transformation, and destination service URIs.
     *
     * Route configuration includes:
     * - Path-based routing with wildcard support
     * - Path rewriting to remove API prefixes
     * - Request header enrichment for monitoring
     * - Load balancing through service discovery
     *
     * @param builder RouteLocatorBuilder for constructing routes
     * @return RouteLocator containing all configured routes
     */
    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("technology-portfolio") { route ->
                route
                    .path("/api/portfolio/**")
                    .filters { filter ->
                        filter
                            .rewritePath("/api/portfolio/(?<segment>.*)", "/\${segment}")
                            .addRequestHeader("X-Response-Time", System.currentTimeMillis().toString())
                    }
                    .uri("lb://technology-portfolio-service")
            }
            .route("authorization") { route ->
                route
                    .path("/api/auth/**")
                    .filters { filter ->
                        filter
                            .rewritePath("/api/auth/(?<segment>.*)", "/\${segment}")
                            .addRequestHeader("X-Response-Time", System.currentTimeMillis().toString())
                    }
                    .uri("lb://authorization-service")
            }
            .route("user-management") { route ->
                route
                    .path("/api/users/**")
                    .filters { filter ->
                        filter
                            .rewritePath("/api/users/(?<segment>.*)", "/\${segment}")
                            .addRequestHeader("X-Response-Time", System.currentTimeMillis().toString())
                    }
                    .uri("lb://user-management-service")
            }
            .route("audit") { route ->
                route
                    .path("/api/audit/**")
                    .filters { filter ->
                        filter
                            .rewritePath("/api/audit/(?<segment>.*)", "/\${segment}")
                            .addRequestHeader("X-Response-Time", System.currentTimeMillis().toString())
                    }
                    .uri("lb://audit-service")
            }
            .route("dashboard") { route ->
                route
                    .path("/dashboard/**")
                    .filters { filter ->
                        filter
                            .addRequestHeader("X-Response-Time", System.currentTimeMillis().toString())
                    }
                    .uri("http://localhost:3000") // Frontend application
            }
            .build()
    }
} 