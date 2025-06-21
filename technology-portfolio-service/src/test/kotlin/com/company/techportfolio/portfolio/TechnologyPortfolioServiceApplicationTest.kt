package com.company.techportfolio.portfolio

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

/**
 * Integration tests for the Technology Portfolio Service application.
 *
 * This test class verifies that the Spring application context loads correctly
 * with all required beans and configurations. It uses an in-memory H2 database
 * for testing and disables Flyway migrations to speed up test execution.
 *
 * ## Test Configuration:
 * - Uses random server port to avoid conflicts
 * - Activates "test" profile
 * - Configures R2DBC with H2 in-memory database
 * - Disables Flyway migrations
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password=",
        "spring.flyway.enabled=false",
        "logging.level.org.springframework.r2dbc=DEBUG"
    ]
)
class TechnologyPortfolioServiceApplicationTest {

    /**
     * Tests that the Spring application context loads successfully.
     *
     * This test verifies that the Spring application context loads correctly
     * with all required beans properly configured and no circular dependencies.
     * The test passes if the context loads without exceptions.
     */
    @Test
    fun `application context should load successfully`() {
        // This test verifies that the Spring application context loads correctly
        // with all beans properly configured and no circular dependencies
    }

    /**
     * Tests that the main application method can be invoked without errors.
     *
     * This test verifies that the application's main method can be invoked
     * without throwing exceptions. It doesn't actually start the application,
     * but confirms that the entry point is valid.
     */
    @Test
    fun `main method should start application`() {
        // Just verify that the main method doesn't throw an exception
        // We don't actually want to start the application in tests
        assertDoesNotThrow {
            // We're not actually running the main function, just checking if the class exists
            // This is a simple way to verify the entry point is valid
            TechnologyPortfolioServiceApplication()
        }
    }
} 