package com.company.techportfolio.gateway.config

import com.company.techportfolio.gateway.domain.model.RouteDefinition
import com.company.techportfolio.gateway.domain.model.RouteFilter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Unit test for [RouteConfiguration].
 *
 * This test verifies the property binding, default values, and data class features
 * of the [RouteConfiguration] class, which is used for externalized API Gateway route configuration.
 *
 * Coverage:
 * - Property binding from configuration
 * - Default values for routes
 * - Data class equality, copy, and toString
 * - Edge cases (empty and null routes)
 *
 * Approach:
 * - Uses ApplicationContextRunner with EnableConfigurationProperties
 * - Tests property binding from configuration files
 * - Uses direct instantiation for data class tests
 */
class RouteConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(TestConfig::class.java)

    @Test
    /**
     * Verifies that RouteConfiguration correctly binds properties from configuration files.
     * Tests that routes defined in YAML configuration are properly loaded into the object.
     */
    fun `should bind properties from configuration`() {
        contextRunner
            .withPropertyValues(
                "gateway.routes[0].id=test-route",
                "gateway.routes[0].path=/test/**",
                "gateway.routes[0].uri=http://test-service",
                "gateway.routes[0].enabled=true",
                "gateway.routes[0].order=1"
            )
            .run { context ->
                val routeConfig = context.getBean(RouteConfiguration::class.java)
                assertNotNull(routeConfig)
                assertEquals(1, routeConfig.routes.size)
                
                val route = routeConfig.routes.first()
                assertEquals("test-route", route.id)
                assertEquals("/test/**", route.path)
                assertEquals("http://test-service", route.uri)
                assertTrue(route.enabled)
                assertEquals(1, route.order)
            }
    }

    @Test
    /**
     * Verifies that RouteConfiguration provides sensible default values when no routes are configured.
     * Tests the default behavior when the configuration is empty or missing.
     */
    fun `should have default empty routes`() {
        contextRunner
            .run { context ->
                val routeConfig = context.getBean(RouteConfiguration::class.java)
                assertNotNull(routeConfig)
                assertTrue(routeConfig.routes.isEmpty())
            }
    }

    @Test
    /**
     * Verifies that RouteConfiguration correctly handles multiple routes from configuration.
     * Tests that multiple route definitions are properly bound and stored.
     */
    fun `should bind multiple routes`() {
        contextRunner
            .withPropertyValues(
                "gateway.routes[0].id=route1",
                "gateway.routes[0].path=/api/v1/**",
                "gateway.routes[0].uri=http://service1",
                "gateway.routes[1].id=route2",
                "gateway.routes[1].path=/api/v2/**",
                "gateway.routes[1].uri=http://service2"
            )
            .run { context ->
                val routeConfig = context.getBean(RouteConfiguration::class.java)
                assertEquals(2, routeConfig.routes.size)
                
                val route1 = routeConfig.routes.find { it.id == "route1" }
                assertNotNull(route1)
                assertEquals("/api/v1/**", route1!!.path)
                assertEquals("http://service1", route1.uri)
                
                val route2 = routeConfig.routes.find { it.id == "route2" }
                assertNotNull(route2)
                assertEquals("/api/v2/**", route2!!.path)
                assertEquals("http://service2", route2.uri)
            }
    }

    @Test
    /**
     * Verifies that RouteConfiguration data class features work correctly.
     * Tests equality, copy operations, and toString functionality.
     */
    fun `should support data class operations`() {
        val route1 = RouteDefinition(
            id = "test",
            path = "/test/**",
            uri = "http://test-service"
        )
        val route2 = RouteDefinition(
            id = "test",
            path = "/test/**",
            uri = "http://test-service"
        )
        
        val config1 = RouteConfiguration(listOf(route1))
        val config2 = RouteConfiguration(listOf(route2))
        
        // Test equality
        assertEquals(config1, config2)
        
        // Test copy
        val config3 = config1.copy(routes = emptyList())
        assertNotEquals(config1, config3)
        assertTrue(config3.routes.isEmpty())
        
        // Test toString
        assertNotNull(config1.toString())
        assertTrue(config1.toString().contains("test"))
    }

    @Test
    /**
     * Verifies that RouteConfiguration correctly binds complex properties including filters.
     * Tests that nested filter configurations are properly bound from properties.
     */
    fun `should bind properties to RouteConfiguration`() {
        contextRunner
            .withPropertyValues(
                "gateway.routes[0].id=test-route",
                "gateway.routes[0].path=/test",
                "gateway.routes[0].uri=http://test-service",
                "gateway.routes[0].enabled=true",
                "gateway.routes[0].order=1",
                "gateway.routes[0].filters[0].type=add-request-header",
                "gateway.routes[0].filters[0].args.name=X-Test",
                "gateway.routes[0].filters[0].args.value=123"
            )
            .run { context ->
                val config = context.getBean(RouteConfiguration::class.java)
                assertEquals(1, config.routes.size)
                val route = config.routes[0]
                assertEquals("test-route", route.id)
                assertEquals("/test", route.path)
                assertEquals("http://test-service", route.uri)
                assertTrue(route.enabled)
                assertEquals(1, route.order)
                assertEquals(1, route.filters.size)
                val filter = route.filters[0]
                assertEquals("add-request-header", filter.type)
                assertEquals("X-Test", filter.args["name"])
                assertEquals("123", filter.args["value"])
            }
    }

    @Test
    /**
     * Verifies that RouteDefinition provides sensible default values for optional properties.
     * Tests the default behavior when optional properties are not specified.
     */
    fun `should use default values`() {
        val route = RouteDefinition(
            id = "id",
            path = "/path",
            uri = "http://uri"
        )
        assertTrue(route.enabled)
        assertEquals(0, route.order)
        assertTrue(route.filters.isEmpty())
        assertTrue(route.metadata.isEmpty())
    }

    @Configuration
    @EnableConfigurationProperties(RouteConfiguration::class)
    class TestConfig
} 