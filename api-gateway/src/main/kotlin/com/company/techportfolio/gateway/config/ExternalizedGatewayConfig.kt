package com.company.techportfolio.gateway.config

import com.company.techportfolio.gateway.domain.model.RouteDefinition
import com.company.techportfolio.gateway.domain.model.RouteFilter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Externalized API Gateway configuration that builds routes from configuration properties.
 *
 * This configuration class reads route definitions from YAML configuration files
 * and dynamically builds Spring Cloud Gateway routes. This allows for:
 * - Environment-specific routing configurations
 * - Easy route management without code changes
 * - Dynamic route enabling/disabling
 * - Configurable filters and metadata
 *
 * Route configuration is defined in application-{profile}.yml files under
 * the `gateway.routes` property.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Configuration
@Profile("!mock-auth")  // Only load when NOT in mock-auth mode
@ConditionalOnProperty(name = ["spring.cloud.gateway.enabled"], havingValue = "true", matchIfMissing = true)
class ExternalizedGatewayConfig(
    private val routeConfiguration: RouteConfiguration,
    private val routeLocatorBuilder: RouteLocatorBuilder,
    private val addRequestHeaderFilterFactory: AddRequestHeaderGatewayFilterFactory,
    private val rewritePathFilterFactory: RewritePathGatewayFilterFactory
) {

    private val logger = LoggerFactory.getLogger(ExternalizedGatewayConfig::class.java)

    /**
     * Builds a RouteLocator from externalized route configuration.
     *
     * Reads route definitions from configuration and builds Spring Cloud Gateway
     * routes with appropriate filters and metadata. Routes are ordered by their
     * configured order property.
     *
     * Supported filter types:
     * - rewrite-path: Rewrites request paths
     * - add-request-header: Adds headers to requests
     * - add-response-header: Adds headers to responses
     * - strip-prefix: Removes path prefixes
     * - circuit-breaker: Applies circuit breaker pattern
     * - retry: Configures retry behavior
     *
     * @return RouteLocator containing all configured routes
     */
    @Bean
    fun externalizedRouteLocator(): RouteLocator {
        logger.info("Building externalized routes from configuration: ${routeConfiguration.routes.size} routes found")

        val routeBuilder = routeLocatorBuilder.routes()

        routeConfiguration.routes
            .filter { it.enabled }
            .sortedBy { it.order }
            .forEach { routeDef ->
                logger.debug("Configuring route: ${routeDef.id} -> ${routeDef.uri}")
                
                routeBuilder.route(routeDef.id) { route ->
                    route.path(routeDef.path)
                        .filters { filterSpec ->
                            var spec = filterSpec
                            routeDef.filters.forEach { filter ->
                                when (filter.type.lowercase()) {
                                    "rewrite-path" -> {
                                        val regex = filter.args["regex"] ?: ""
                                        val replacement = filter.args["replacement"] ?: ""
                                        spec = spec.rewritePath(regex, replacement)
                                    }
                                    "add-request-header" -> {
                                        val name = filter.args["name"] ?: ""
                                        val value = filter.args["value"] ?: ""
                                        spec = spec.addRequestHeader(name, value)
                                    }
                                    "add-response-header" -> {
                                        val name = filter.args["name"] ?: ""
                                        val value = filter.args["value"] ?: ""
                                        spec = spec.addResponseHeader(name, value)
                                    }
                                    "strip-prefix" -> {
                                        val parts = filter.args["parts"]?.toIntOrNull() ?: 1
                                        spec = spec.stripPrefix(parts)
                                    }
                                    "circuit-breaker" -> {
                                        val name = filter.args["name"] ?: "default"
                                        spec = spec.circuitBreaker { config ->
                                            config.setName(name)
                                        }
                                    }
                                    "retry" -> {
                                        val retries = filter.args["retries"]?.toIntOrNull() ?: 3
                                        // For simplicity, do not set statuses here; just set retries
                                        spec = spec.retry { config ->
                                            config.setRetries(retries)
                                        }
                                    }
                                    else -> {
                                        logger.warn("Unknown filter type: ${filter.type}")
                                    }
                                }
                            }
                            spec
                        }
                        .uri(routeDef.uri)
                }
            }

        return routeBuilder.build()
    }
} 