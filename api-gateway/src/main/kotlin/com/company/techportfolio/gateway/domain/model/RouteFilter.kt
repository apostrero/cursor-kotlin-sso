package com.company.techportfolio.gateway.domain.model

/**
 * Definition of a route filter for the API Gateway.
 *
 * This data class represents a filter configuration that can be applied to routes for
 * request/response transformation, monitoring, and security purposes. Filters are the
 * mechanism through which the API Gateway can modify requests and responses as they
 * flow through the system.
 *
 * Filters are applied in the order they appear in the route's filter list, allowing
 * for complex transformation chains. Each filter type has specific arguments that
 * configure its behavior.
 *
 * Supported filter types:
 * - `rewrite-path`: Rewrites request paths using regex patterns
 * - `add-request-header`: Adds headers to outgoing requests
 * - `add-response-header`: Adds headers to incoming responses
 * - `strip-prefix`: Removes path prefixes from requests
 * - `circuit-breaker`: Applies circuit breaker pattern for fault tolerance
 * - `retry`: Configures retry behavior for failed requests
 * - `rate-limiter`: Limits request rates
 * - `request-size`: Limits request body size
 * - `modify-response-body`: Modifies response body content
 *
 * Usage examples:
 * ```
 * // Add request header
 * RouteFilter(
 *     type = "add-request-header",
 *     args = mapOf("name" to "X-Source", "value" to "gateway")
 * )
 *
 * // Rewrite path
 * RouteFilter(
 *     type = "rewrite-path",
 *     args = mapOf(
 *         "regex" to "/api/users/(?<segment>.*)",
 *         "replacement" to "/\${segment}"
 *     )
 * )
 *
 * // Circuit breaker
 * RouteFilter(
 *     type = "circuit-breaker",
 *     args = mapOf("name" to "user-service-circuit-breaker")
 * )
 *
 * // Retry with specific configuration
 * RouteFilter(
 *     type = "retry",
 *     args = mapOf(
 *         "retries" to "3",
 *         "statuses" to "5XX,502,503"
 *     )
 * )
 * ```
 *
 * Filter arguments are always string-based to support external configuration
 * through YAML files. Numeric values, booleans, and other types should be
 * converted to strings when provided in configuration.
 *
 * Common argument patterns:
 * - Header filters: `name` and `value` for header name and value
 * - Path filters: `regex` and `replacement` for path rewriting
 * - Circuit breaker: `name` for the circuit breaker instance name
 * - Retry filters: `retries` for number of retries, `statuses` for HTTP status codes
 * - Rate limiting: `requests-per-second` for rate limit configuration
 *
 * @property type Type of filter to apply. Must be one of the supported filter types
 *               recognized by Spring Cloud Gateway. Filter types are case-sensitive.
 * @property args Arguments for the filter as key-value pairs. All values are strings
 *               to support external configuration. The specific arguments depend on
 *               the filter type being used.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see RouteDefinition
 * @see ExternalizedGatewayConfig
 */
data class RouteFilter(
    val type: String,
    val args: Map<String, String> = emptyMap()
) 