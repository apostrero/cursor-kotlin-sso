package com.company.techportfolio.gateway.domain.model

import com.company.techportfolio.gateway.domain.model.RouteFilter

/**
 * Definition of a single gateway route for the API Gateway.
 *
 * This data class represents a route configuration that defines how incoming HTTP requests
 * should be routed to backend services. Routes are the core building blocks of the API Gateway's
 * routing system, allowing for flexible and configurable request routing based on path patterns.
 *
 * Each route consists of:
 * - A unique identifier for management and monitoring
 * - A path pattern to match incoming requests
 * - A destination URI for the target service
 * - Optional filters for request/response transformation
 * - Metadata for operational purposes
 * - Configuration flags for enabling/disabling and ordering
 *
 * Route ordering is important when multiple routes could match the same request path.
 * Routes with lower order values have higher priority and are evaluated first.
 *
 * Usage examples:
 * ```
 * // Simple route to a service
 * RouteDefinition(
 *     id = "user-service",
 *     path = "/api/users/##",
 *     uri = "lb://user-service"
 * )
 *
 * // Route with filters and metadata
 * RouteDefinition(
 *     id = "portfolio-service",
 *     path = "/api/portfolio/##",
 *     uri = "lb://portfolio-service",
 *     filters = listOf(
 *         RouteFilter("rewrite-path", mapOf("regex" to "/api/portfolio/(?<segment>.*)", "replacement" to "/\${segment}")),
 *         RouteFilter("add-request-header", mapOf("name" to "X-Source", "value" to "gateway"))
 *     ),
 *     metadata = mapOf("service" to "portfolio", "environment" to "production"),
 *     order = 1
 * )
 * ```
 *
 * Path patterns support:
 * - Exact matches: `/api/users`
 * - Wildcard patterns: `/api/users/##`
 * - Path variables: `/api/users/{id}`
 * - Ant-style patterns: `/api/users/$/profile`
 *
 * URI formats supported:
 * - HTTP/HTTPS: `http://localhost:8080`, `https://api.example.com`
 * - Service discovery: `lb://service-name` (load balanced)
 * - WebSocket: `ws://localhost:8080/ws`
 *
 * @property id Unique identifier for the route, used for management, monitoring, and debugging.
 *              Must be unique across all routes in the gateway configuration.
 * @property path Path pattern to match incoming requests. Supports various pattern types
 *               including exact matches, wildcards, and path variables.
 * @property uri Destination URI for the route. Can be a direct HTTP/HTTPS URL or a
 *              service discovery reference (e.g., `lb://service-name`).
 * @property filters List of filters to apply to the route for request/response transformation.
 *                  Filters are applied in the order they appear in the list.
 * @property metadata Additional metadata for the route, useful for monitoring, logging,
 *                    and operational purposes. Key-value pairs that don't affect routing logic.
 * @property enabled Whether the route is enabled and should be active. Disabled routes
 *                  are ignored during route matching.
 * @property order Route ordering (lower numbers have higher priority). Used to determine
 *                which route to use when multiple routes could match the same request.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see RouteFilter
 * @see RouteConfiguration
 */
data class RouteDefinition(
    val id: String,
    val path: String,
    val uri: String,
    val filters: List<RouteFilter> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val enabled: Boolean = true,
    val order: Int = 0
) 