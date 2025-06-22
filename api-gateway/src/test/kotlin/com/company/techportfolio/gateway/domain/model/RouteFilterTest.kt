package com.company.techportfolio.gateway.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit test for [RouteFilter].
 *
 * This test verifies the data class features, property handling, and edge cases
 * for the [RouteFilter] model, which represents a filter applied to an API Gateway route.
 *
 * Coverage:
 * - All properties and default values
 * - Data class equality, copy, and toString
 * - Edge cases (empty args, unusual filter types)
 * - Real-world filter configuration scenarios
 *
 * Approach:
 * - Pure unit test, no Spring context required
 * - Uses direct instantiation and assertions
 *
 * Test Coverage:
 * - Basic instantiation with all properties
 * - Default value handling for optional arguments
 * - Data class functionality (equality, copy, toString)
 * - Edge cases and boundary conditions
 * - Real-world filter scenarios
 * - String handling (empty strings, whitespace, special characters, unicode)
 * - Complex argument patterns (regex, numeric values, boolean values)
 * - Case sensitivity and special characters in filter types
 * - Multiple values and complex argument structures
 *
 * Testing Strategy:
 * - Each test focuses on a specific aspect of the RouteFilter class
 * - Tests use descriptive names that clearly indicate what is being tested
 * - Edge cases are covered to ensure robust behavior
 * - Real-world scenarios are tested to validate practical usage
 * - Data class contract is verified (equality, copy, toString)
 * - String handling edge cases are thoroughly tested
 * - Complex argument patterns are validated
 *
 * The tests ensure that RouteFilter works correctly in all scenarios where it
 * might be used, including external configuration binding, programmatic creation,
 * and integration with Spring Cloud Gateway filter factories.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see RouteFilter
 * @see RouteDefinition
 */
class RouteFilterTest {

    /**
     * Tests that a RouteFilter can be created with all properties specified.
     * This verifies that the constructor accepts all parameters and assigns them correctly.
     */
    @Test
    fun `should create route filter with all properties`() {
        val args = mapOf(
            "name" to "X-Test-Header",
            "value" to "test-value",
            "regex" to "/api/(?<segment>.*)",
            "replacement" to "/\${segment}"
        )
        
        val filter = RouteFilter(
            type = "add-request-header",
            args = args
        )
        
        assertEquals("add-request-header", filter.type)
        assertEquals(args, filter.args)
        assertEquals("X-Test-Header", filter.args["name"])
        assertEquals("test-value", filter.args["value"])
        assertEquals("/api/(?<segment>.*)", filter.args["regex"])
        assertEquals("/\${segment}", filter.args["replacement"])
    }

    /**
     * Tests that RouteFilter uses correct default values when optional arguments
     * are not provided. This ensures backward compatibility and ease of use.
     */
    @Test
    fun `should use default empty map when args are not provided`() {
        val filter = RouteFilter(type = "simple-filter")
        
        assertEquals("simple-filter", filter.type)
        assertTrue(filter.args.isEmpty())
        assertEquals(0, filter.args.size)
    }

    /**
     * Tests that RouteFilter supports proper equality comparison and hashCode generation.
     * This is essential for data class functionality and collection operations.
     */
    @Test
    fun `should support equality comparison`() {
        val filter1 = RouteFilter(
            type = "add-header",
            args = mapOf("name" to "X-Test", "value" to "123")
        )
        
        val filter2 = RouteFilter(
            type = "add-header",
            args = mapOf("name" to "X-Test", "value" to "123")
        )
        
        assertEquals(filter1, filter2)
        assertEquals(filter1.hashCode(), filter2.hashCode())
    }

    /**
     * Tests that RouteFilter supports the copy functionality, allowing immutable
     * updates to filter configurations.
     */
    @Test
    fun `should support copy functionality`() {
        val original = RouteFilter(
            type = "original-filter",
            args = mapOf("original" to "value")
        )
        
        val copied = original.copy(
            type = "copied-filter",
            args = mapOf("copied" to "new-value")
        )
        
        assertEquals("copied-filter", copied.type)
        assertEquals(mapOf("copied" to "new-value"), copied.args)
    }

    /**
     * Tests that RouteFilter provides a meaningful toString representation
     * for debugging and logging purposes.
     */
    @Test
    fun `should support toString representation`() {
        val filter = RouteFilter(
            type = "test-filter",
            args = mapOf("name" to "X-Test", "value" to "123")
        )
        
        val toString = filter.toString()
        
        assertTrue(toString.contains("test-filter"))
        assertTrue(toString.contains("name"))
        assertTrue(toString.contains("X-Test"))
        assertTrue(toString.contains("value"))
        assertTrue(toString.contains("123"))
    }

    /**
     * Tests that RouteFilter handles empty arguments map correctly.
     * This is a common scenario for filters that don't require configuration.
     */
    @Test
    fun `should handle empty args map`() {
        val filter = RouteFilter(
            type = "empty-args-filter",
            args = emptyMap()
        )
        
        assertEquals("empty-args-filter", filter.type)
        assertTrue(filter.args.isEmpty())
        assertEquals(0, filter.args.size)
    }

    /**
     * Tests that RouteFilter handles case-sensitive filter types correctly.
     * This validates that filter types preserve their exact case.
     */
    @Test
    fun `should handle case sensitive filter types`() {
        val filter = RouteFilter(
            type = "Add-Request-Header",
            args = mapOf("name" to "X-Test")
        )
        
        assertEquals("Add-Request-Header", filter.type)
    }

    /**
     * Tests that RouteFilter handles special characters in filter type names.
     * This validates support for custom filter types with special naming.
     */
    @Test
    fun `should handle special characters in filter type`() {
        val filter = RouteFilter(
            type = "custom-filter-123",
            args = mapOf("custom" to "value")
        )
        
        assertEquals("custom-filter-123", filter.type)
    }

    /**
     * Tests that RouteFilter handles complex regex patterns in arguments correctly.
     * This validates support for advanced path rewriting scenarios.
     */
    @Test
    fun `should handle complex regex patterns in args`() {
        val complexRegex = "/api/(?<service>[a-zA-Z]+)/(?<version>v\\d+)/(?<resource>.*)"
        val complexReplacement = "/\${service}/\${version}/\${resource}"
        
        val filter = RouteFilter(
            type = "rewrite-path",
            args = mapOf(
                "regex" to complexRegex,
                "replacement" to complexReplacement
            )
        )
        
        assertEquals(complexRegex, filter.args["regex"])
        assertEquals(complexReplacement, filter.args["replacement"])
    }

    /**
     * Tests that RouteFilter handles numeric values in arguments correctly.
     * This validates support for numeric configuration parameters.
     */
    @Test
    fun `should handle numeric values in args`() {
        val filter = RouteFilter(
            type = "strip-prefix",
            args = mapOf(
                "parts" to "2",
                "retries" to "3",
                "timeout" to "5000"
            )
        )
        
        assertEquals("2", filter.args["parts"])
        assertEquals("3", filter.args["retries"])
        assertEquals("5000", filter.args["timeout"])
    }

    /**
     * Tests that RouteFilter handles boolean values in arguments correctly.
     * This validates support for boolean configuration parameters.
     */
    @Test
    fun `should handle boolean values in args`() {
        val filter = RouteFilter(
            type = "circuit-breaker",
            args = mapOf(
                "enabled" to "true",
                "fallback" to "false"
            )
        )
        
        assertEquals("true", filter.args["enabled"])
        assertEquals("false", filter.args["fallback"])
    }

    /**
     * Tests that RouteFilter handles multiple values for the same key correctly.
     * This validates support for comma-separated configuration values.
     */
    @Test
    fun `should handle multiple values for same key`() {
        val filter = RouteFilter(
            type = "retry",
            args = mapOf(
                "statuses" to "5XX,502,503,504",
                "methods" to "GET,POST,PUT"
            )
        )
        
        assertEquals("5XX,502,503,504", filter.args["statuses"])
        assertEquals("GET,POST,PUT", filter.args["methods"])
    }

    /**
     * Tests that RouteFilter handles empty string values in arguments correctly.
     * This validates support for empty configuration values.
     */
    @Test
    fun `should handle empty string values in args`() {
        val filter = RouteFilter(
            type = "add-header",
            args = mapOf(
                "name" to "X-Empty",
                "value" to ""
            )
        )
        
        assertEquals("X-Empty", filter.args["name"])
        assertEquals("", filter.args["value"])
    }

    /**
     * Tests that RouteFilter handles whitespace in argument values correctly.
     * This validates support for configuration values with leading/trailing spaces.
     */
    @Test
    fun `should handle whitespace in args values`() {
        val filter = RouteFilter(
            type = "add-header",
            args = mapOf(
                "name" to "X-Whitespace",
                "value" to "  spaced value  "
            )
        )
        
        assertEquals("X-Whitespace", filter.args["name"])
        assertEquals("  spaced value  ", filter.args["value"])
    }

    /**
     * Tests that RouteFilter handles special characters in argument values correctly.
     * This validates support for configuration values with special characters.
     */
    @Test
    fun `should handle special characters in args values`() {
        val filter = RouteFilter(
            type = "add-header",
            args = mapOf(
                "name" to "X-Special",
                "value" to "!@#$%^&*()_+-=[]{}|;':\",./<>?"
            )
        )
        
        assertEquals("X-Special", filter.args["name"])
        assertEquals("!@#$%^&*()_+-=[]{}|;':\",./<>?", filter.args["value"])
    }

    /**
     * Tests that RouteFilter handles unicode characters in argument values correctly.
     * This validates support for internationalized configuration values.
     */
    @Test
    fun `should handle unicode characters in args values`() {
        val filter = RouteFilter(
            type = "add-header",
            args = mapOf(
                "name" to "X-Unicode",
                "value" to "🚀🌟✨🎉"
            )
        )
        
        assertEquals("X-Unicode", filter.args["name"])
        assertEquals("🚀🌟✨🎉", filter.args["value"])
    }

    @Test
    /**
     * Verifies that RouteFilter correctly handles all required properties.
     * Tests that the data class properly stores and exposes filter type and arguments.
     */
    fun `should have all required properties`() {
        val filter = RouteFilter(
            type = "add-request-header",
            args = mapOf("name" to "X-Test", "value" to "123")
        )

        assertEquals("add-request-header", filter.type)
        assertEquals(2, filter.args.size)
        assertEquals("X-Test", filter.args["name"])
        assertEquals("123", filter.args["value"])
    }

    @Test
    /**
     * Verifies that RouteFilter provides sensible default values for optional properties.
     * Tests that when optional properties are not specified, appropriate defaults are used.
     */
    fun `should have default values for optional properties`() {
        val filter = RouteFilter(
            type = "rewrite-path"
        )

        assertTrue(filter.args.isEmpty())
    }

    @Test
    /**
     * Verifies that RouteFilter data class features work correctly.
     * Tests equality, copy operations, and toString functionality.
     */
    fun `should support data class operations`() {
        val filter1 = RouteFilter(
            type = "add-request-header",
            args = mapOf("name" to "X-Test", "value" to "123")
        )
        val filter2 = RouteFilter(
            type = "add-request-header",
            args = mapOf("name" to "X-Test", "value" to "123")
        )

        // Test equality
        assertEquals(filter1, filter2)
        assertEquals(filter1.hashCode(), filter2.hashCode())

        // Test copy
        val filter3 = filter1.copy(type = "add-response-header")
        assertNotEquals(filter1, filter3)
        assertEquals("add-response-header", filter3.type)

        // Test toString
        assertNotNull(filter1.toString())
        assertTrue(filter1.toString().contains("add-request-header"))
    }

    @Test
    /**
     * Verifies that RouteFilter correctly handles edge cases with empty or unusual data.
     * Tests behavior when arguments are empty and filter types are unusual.
     */
    fun `should handle edge cases`() {
        val emptyFilter = RouteFilter(
            type = "custom-filter",
            args = emptyMap()
        )

        assertEquals("custom-filter", emptyFilter.type)
        assertTrue(emptyFilter.args.isEmpty())

        val unusualFilter = RouteFilter(
            type = "very-long-and-unusual-filter-type-name",
            args = mapOf("very-long-key" to "very-long-value")
        )

        assertEquals("very-long-and-unusual-filter-type-name", unusualFilter.type)
        assertEquals("very-long-value", unusualFilter.args["very-long-key"])
    }

    @Test
    /**
     * Verifies that RouteFilter correctly handles real-world filter configuration scenarios.
     * Tests realistic filter configurations used in API Gateway routing.
     */
    fun `should handle real-world filter configurations`() {
        // Rewrite path filter
        val rewriteFilter = RouteFilter(
            type = "rewrite-path",
            args = mapOf(
                "regex" to "/api/portfolio/(?<segment>.*)",
                "replacement" to "/\${segment}"
            )
        )

        assertEquals("rewrite-path", rewriteFilter.type)
        assertEquals("/api/portfolio/(?<segment>.*)", rewriteFilter.args["regex"])
        assertEquals("/\${segment}", rewriteFilter.args["replacement"])

        // Circuit breaker filter
        val circuitBreakerFilter = RouteFilter(
            type = "circuit-breaker",
            args = mapOf(
                "name" to "portfolio-circuit",
                "fallback-uri" to "forward:/fallback"
            )
        )

        assertEquals("circuit-breaker", circuitBreakerFilter.type)
        assertEquals("portfolio-circuit", circuitBreakerFilter.args["name"])
        assertEquals("forward:/fallback", circuitBreakerFilter.args["fallback-uri"])

        // Retry filter
        val retryFilter = RouteFilter(
            type = "retry",
            args = mapOf(
                "retries" to "3",
                "statuses" to "BAD_GATEWAY,INTERNAL_SERVER_ERROR"
            )
        )

        assertEquals("retry", retryFilter.type)
        assertEquals("3", retryFilter.args["retries"])
        assertEquals("BAD_GATEWAY,INTERNAL_SERVER_ERROR", retryFilter.args["statuses"])
    }

    @Test
    /**
     * Verifies that RouteFilter correctly handles different filter types commonly used in API Gateway.
     * Tests various filter types to ensure they are properly stored and retrieved.
     */
    fun `should handle different filter types`() {
        val filters = listOf(
            RouteFilter("add-request-header", mapOf("name" to "X-Test", "value" to "123")),
            RouteFilter("add-response-header", mapOf("name" to "X-Response-Time", "value" to "timestamp")),
            RouteFilter("strip-prefix", mapOf("parts" to "2")),
            RouteFilter("rewrite-path", mapOf("regex" to "/old/(.*)", "replacement" to "/new/\${1}")),
            RouteFilter("circuit-breaker", mapOf("name" to "default")),
            RouteFilter("retry", mapOf("retries" to "3")),
            RouteFilter("rate-limiter", mapOf("requests-per-second" to "10")),
            RouteFilter("request-size", mapOf("max-size" to "5MB"))
        )

        assertEquals(8, filters.size)
        
        // Verify each filter type is stored correctly
        assertTrue(filters.any { it.type == "add-request-header" })
        assertTrue(filters.any { it.type == "add-response-header" })
        assertTrue(filters.any { it.type == "strip-prefix" })
        assertTrue(filters.any { it.type == "rewrite-path" })
        assertTrue(filters.any { it.type == "circuit-breaker" })
        assertTrue(filters.any { it.type == "retry" })
        assertTrue(filters.any { it.type == "rate-limiter" })
        assertTrue(filters.any { it.type == "request-size" })
    }
} 