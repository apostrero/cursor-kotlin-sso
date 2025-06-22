package com.company.techportfolio.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import com.company.techportfolio.gateway.domain.model.RouteDefinition

/**
 * Configuration properties for API Gateway routes.
 *
 * This class defines the structure for externalized route configuration,
 * allowing routes to be defined in YAML configuration files instead of
 * hardcoded in Java/Kotlin code.
 *
 * Route configuration includes:
 * - Route ID and path patterns
 * - Destination service URIs
 * - Filter configurations
 * - Metadata for monitoring and management
 *
 * @property routes List of configured routes
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "gateway")
data class RouteConfiguration(
    var routes: List<RouteDefinition> = emptyList()
) 
 