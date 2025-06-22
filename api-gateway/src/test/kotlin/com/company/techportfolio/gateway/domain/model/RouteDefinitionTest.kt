package com.company.techportfolio.gateway.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit test for [RouteDefinition].
 *
 * This test verifies the data class features, property handling, and edge cases
 * for the [RouteDefinition] model, which represents a single API Gateway route definition.
 *
 * Coverage:
 * - All properties and default values
 * - Data class equality, copy, and toString
 * - Edge cases (empty filters, missing metadata)
 * - Real-world configuration scenarios
 *
 * Approach:
 * - Pure unit test, no Spring context required
 * - Uses direct instantiation and assertions
 *
 * Test suite for the RouteDefinition data class.
 *
 * This test class provides comprehensive coverage for the RouteDefinition data class,
 * which is a core component of the API Gateway's externalized route configuration system.
 * The tests verify that RouteDefinition instances are created correctly, handle all
 * property combinations, and support the expected data class functionality.
 *
 * Test Coverage:
 * - Basic instantiation with all properties
 * - Default value handling for optional properties
 * - Data class functionality (equality, copy, toString)
 * - Edge cases and boundary conditions
 * - Real-world usage scenarios
 * - Collection handling (empty lists and maps)
 * - Boolean and numeric property validation
 * - Path pattern and URI format validation
 *
 * Testing Strategy:
 * - Each test focuses on a specific aspect of the RouteDefinition class
 * - Tests use descriptive names that clearly indicate what is being tested
 * - Edge cases are covered to ensure robust behavior
 * - Real-world scenarios are tested to validate practical usage
 * - Data class contract is verified (equality, copy, toString)
 *
 * The tests ensure that RouteDefinition works correctly in all scenarios where it
 * might be used, including external configuration binding, programmatic creation,
 * and integration with the Spring Cloud Gateway framework.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see RouteDefinition
 * @see RouteFilter
 */
class RouteDefinitionTest {

    /**
     * Tests that a RouteDefinition can be created with all properties specified.
     * This verifies that the constructor accepts all parameters and assigns them correctly.
     */
    @Test
    fun `should create route definition with all properties`() {
        val filters = listOf(
            RouteFilter("add-request-header", mapOf("name" to "X-Test", "value" to "123"))
        )
        val metadata = mapOf("service" to "test-service", "environment" to "dev")
        
        val route = RouteDefinition(
            id = "test-route",
            path = "/api/test/**",
            uri = "lb://test-service",
            filters = filters,
            metadata = metadata,
            enabled = true,
            order = 1
        )
        
        assertEquals("test-route", route.id)
        assertEquals("/api/test/**", route.path)
        assertEquals("lb://test-service", route.uri)
        assertEquals(filters, route.filters)
        assertEquals(metadata, route.metadata)
        assertTrue(route.enabled)
        assertEquals(1, route.order)
    }

    /**
     * Tests that RouteDefinition uses correct default values when optional properties
     * are not provided. This ensures backward compatibility and ease of use.
     */
    @Test
    fun `should use default values when optional properties are not provided`() {
        val route = RouteDefinition(
            id = "minimal-route",
            path = "/api/minimal",
            uri = "http://minimal-service"
        )
        
        assertEquals("minimal-route", route.id)
        assertEquals("/api/minimal", route.path)
        assertEquals("http://minimal-service", route.uri)
        assertTrue(route.filters.isEmpty())
        assertTrue(route.metadata.isEmpty())
        assertTrue(route.enabled)
        assertEquals(0, route.order)
    }

    /**
     * Tests that RouteDefinition supports proper equality comparison and hashCode generation.
     * This is essential for data class functionality and collection operations.
     */
    @Test
    fun `should support equality comparison`() {
        val route1 = RouteDefinition(
            id = "test-route",
            path = "/api/test",
            uri = "http://test-service",
            filters = listOf(RouteFilter("add-header", mapOf("name" to "X-Test"))),
            metadata = mapOf("env" to "test"),
            enabled = true,
            order = 1
        )
        
        val route2 = RouteDefinition(
            id = "test-route",
            path = "/api/test",
            uri = "http://test-service",
            filters = listOf(RouteFilter("add-header", mapOf("name" to "X-Test"))),
            metadata = mapOf("env" to "test"),
            enabled = true,
            order = 1
        )
        
        assertEquals(route1, route2)
        assertEquals(route1.hashCode(), route2.hashCode())
    }

    /**
     * Tests that RouteDefinition supports the copy functionality, allowing immutable
     * updates to route configurations.
     */
    @Test
    fun `should support copy functionality`() {
        val original = RouteDefinition(
            id = "original-route",
            path = "/api/original",
            uri = "http://original-service",
            enabled = true,
            order = 1
        )
        
        val copied = original.copy(
            id = "copied-route",
            enabled = false,
            order = 2
        )
        
        assertEquals("copied-route", copied.id)
        assertEquals("/api/original", copied.path) // unchanged
        assertEquals("http://original-service", copied.uri) // unchanged
        assertFalse(copied.enabled)
        assertEquals(2, copied.order)
    }

    /**
     * Tests that RouteDefinition provides a meaningful toString representation
     * for debugging and logging purposes.
     */
    @Test
    fun `should support toString representation`() {
        val route = RouteDefinition(
            id = "test-route",
            path = "/api/test",
            uri = "http://test-service",
            filters = listOf(RouteFilter("add-header", mapOf("name" to "X-Test"))),
            metadata = mapOf("env" to "test"),
            enabled = true,
            order = 1
        )
        
        val toString = route.toString()
        
        assertTrue(toString.contains("test-route"))
        assertTrue(toString.contains("/api/test"))
        assertTrue(toString.contains("http://test-service"))
        assertTrue(toString.contains("enabled=true"))
        assertTrue(toString.contains("order=1"))
    }

    /**
     * Tests that RouteDefinition handles empty filters list correctly.
     * This is a common scenario for simple routes without transformations.
     */
    @Test
    fun `should handle empty filters list`() {
        val route = RouteDefinition(
            id = "empty-filters-route",
            path = "/api/empty-filters",
            uri = "http://empty-filters-service",
            filters = emptyList()
        )
        
        assertTrue(route.filters.isEmpty())
        assertEquals(0, route.filters.size)
    }

    /**
     * Tests that RouteDefinition handles empty metadata map correctly.
     * This is a common scenario for routes without operational metadata.
     */
    @Test
    fun `should handle empty metadata map`() {
        val route = RouteDefinition(
            id = "empty-metadata-route",
            path = "/api/empty-metadata",
            uri = "http://empty-metadata-service",
            metadata = emptyMap()
        )
        
        assertTrue(route.metadata.isEmpty())
        assertEquals(0, route.metadata.size)
    }

    /**
     * Tests that RouteDefinition correctly handles disabled routes.
     * This is important for route management and A/B testing scenarios.
     */
    @Test
    fun `should handle disabled route`() {
        val route = RouteDefinition(
            id = "disabled-route",
            path = "/api/disabled",
            uri = "http://disabled-service",
            enabled = false
        )
        
        assertFalse(route.enabled)
    }

    /**
     * Tests that RouteDefinition accepts negative order values.
     * This allows for high-priority routes with negative order numbers.
     */
    @Test
    fun `should handle negative order`() {
        val route = RouteDefinition(
            id = "negative-order-route",
            path = "/api/negative-order",
            uri = "http://negative-order-service",
            order = -1
        )
        
        assertEquals(-1, route.order)
    }

    /**
     * Tests that RouteDefinition handles complex path patterns correctly.
     * This validates support for advanced routing scenarios.
     */
    @Test
    fun `should handle complex path patterns`() {
        val route = RouteDefinition(
            id = "complex-path-route",
            path = "/api/{service}/{version}/**",
            uri = "lb://complex-service"
        )
        
        assertEquals("/api/{service}/{version}/**", route.path)
    }

    /**
     * Tests that RouteDefinition handles service discovery URIs correctly.
     * This validates support for load-balanced service references.
     */
    @Test
    fun `should handle service discovery URIs`() {
        val route = RouteDefinition(
            id = "service-discovery-route",
            path = "/api/discovery",
            uri = "lb://service-discovery"
        )
        
        assertEquals("lb://service-discovery", route.uri)
    }

    @Test
    /**
     * Verifies that RouteDefinition correctly handles all required properties.
     * Tests that the data class properly stores and exposes all configured route properties.
     */
    fun `should have all required properties`() {
        val route = RouteDefinition(
            id = "test-route",
            path = "/test/**",
            uri = "http://test-service",
            filters = listOf(RouteFilter("add-request-header", mapOf("name" to "X-Test"))),
            enabled = true,
            order = 1,
            metadata = mapOf("environment" to "test")
        )

        assertEquals("test-route", route.id)
        assertEquals("/test/**", route.path)
        assertEquals("http://test-service", route.uri)
        assertEquals(1, route.filters.size)
        assertTrue(route.enabled)
        assertEquals(1, route.order)
        assertEquals("test", route.metadata["environment"])
    }

    @Test
    /**
     * Verifies that RouteDefinition provides sensible default values for optional properties.
     * Tests that when optional properties are not specified, appropriate defaults are used.
     */
    fun `should have default values for optional properties`() {
        val route = RouteDefinition(
            id = "test-route",
            path = "/test/**",
            uri = "http://test-service"
        )

        assertTrue(route.filters.isEmpty())
        assertTrue(route.enabled)
        assertEquals(0, route.order)
        assertTrue(route.metadata.isEmpty())
    }

    @Test
    /**
     * Verifies that RouteDefinition data class features work correctly.
     * Tests equality, copy operations, and toString functionality.
     */
    fun `should support data class operations`() {
        val route1 = RouteDefinition(
            id = "test",
            path = "/test/**",
            uri = "http://test-service",
            enabled = true,
            order = 1
        )
        val route2 = RouteDefinition(
            id = "test",
            path = "/test/**",
            uri = "http://test-service",
            enabled = true,
            order = 1
        )

        // Test equality
        assertEquals(route1, route2)
        assertEquals(route1.hashCode(), route2.hashCode())

        // Test copy
        val route3 = route1.copy(enabled = false)
        assertNotEquals(route1, route3)
        assertFalse(route3.enabled)

        // Test toString
        assertNotNull(route1.toString())
        assertTrue(route1.toString().contains("test"))
    }

    @Test
    /**
     * Verifies that RouteDefinition correctly handles edge cases with empty or minimal data.
     * Tests behavior when filters are empty and metadata is missing.
     */
    fun `should handle edge cases`() {
        val route = RouteDefinition(
            id = "minimal-route",
            path = "/minimal/**",
            uri = "http://minimal-service",
            filters = emptyList(),
            enabled = false,
            order = 999,
            metadata = emptyMap()
        )

        assertTrue(route.filters.isEmpty())
        assertFalse(route.enabled)
        assertEquals(999, route.order)
        assertTrue(route.metadata.isEmpty())
    }

    @Test
    /**
     * Verifies that RouteDefinition correctly handles real-world configuration scenarios.
     * Tests a realistic route configuration with multiple filters and metadata.
     */
    fun `should handle real-world configuration`() {
        val route = RouteDefinition(
            id = "api-portfolio",
            path = "/api/portfolio/**",
            uri = "lb://technology-portfolio-service",
            filters = listOf(
                RouteFilter("rewrite-path", mapOf(
                    "regex" to "/api/portfolio/(?<segment>.*)",
                    "replacement" to "/\${segment}"
                )),
                RouteFilter("add-request-header", mapOf(
                    "name" to "X-Response-Time",
                    "value" to "timestamp"
                )),
                RouteFilter("circuit-breaker", mapOf("name" to "portfolio-circuit"))
            ),
            enabled = true,
            order = 1,
            metadata = mapOf(
                "service" to "technology-portfolio",
                "environment" to "production",
                "version" to "1.0"
            )
        )

        assertEquals("api-portfolio", route.id)
        assertEquals("/api/portfolio/**", route.path)
        assertEquals("lb://technology-portfolio-service", route.uri)
        assertEquals(3, route.filters.size)
        assertTrue(route.enabled)
        assertEquals(1, route.order)
        assertEquals("technology-portfolio", route.metadata["service"])
        assertEquals("production", route.metadata["environment"])
        assertEquals("1.0", route.metadata["version"])
    }
} 