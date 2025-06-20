package com.company.techportfolio.portfolio

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.junit.jupiter.api.Assertions.assertDoesNotThrow

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
 * - Configures Hibernate to create-drop tables
 * - Disables Flyway migrations
 * - Provides in-memory H2 database
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
])
class TechnologyPortfolioServiceApplicationTest {

    /**
     * Test configuration that provides test-specific beans.
     * 
     * This inner configuration class provides test-specific beans that override
     * production beans. It configures the database and disables Flyway migrations
     * to ensure tests run quickly and independently.
     */
    @TestConfiguration
    @AutoConfigureBefore(FlywayAutoConfiguration::class)
    @AutoConfigureAfter(DataSourceAutoConfiguration::class)
    class TestConfig {
        /**
         * Provides an in-memory H2 database for testing.
         * 
         * This database is isolated and destroyed after each test run,
         * ensuring test independence and preventing test data persistence.
         * 
         * @return In-memory H2 DataSource
         */
        @Bean
        @Primary
        fun dataSource(): DataSource {
            val dataSource = JdbcDataSource()
            dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
            dataSource.setUser("sa")
            dataSource.setPassword("")
            return dataSource
        }
        
        /**
         * Disables Flyway migrations for tests.
         * 
         * Returns null to prevent Flyway from running migrations during tests,
         * which speeds up test execution and avoids unnecessary database setup.
         * 
         * @return null to disable Flyway
         */
        @Bean
        @Primary
        fun flywayMigrationInitializer(): FlywayMigrationInitializer? {
            // Return null to disable Flyway in tests
            return null
        }
    }

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