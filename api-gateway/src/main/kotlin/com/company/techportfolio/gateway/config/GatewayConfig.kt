package com.company.techportfolio.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig {

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