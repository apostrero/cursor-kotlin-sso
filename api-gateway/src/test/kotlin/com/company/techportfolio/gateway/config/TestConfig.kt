package com.company.techportfolio.gateway.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate

/**
 * Test configuration class for the API Gateway tests.
 *
 * This configuration provides test-specific beans and overrides production
 * configurations to enable proper test execution without external dependencies.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@TestConfiguration
class TestConfig {

    /**
     * Creates a simple RestTemplate bean for testing.
     *
     * This RestTemplate is created without RestTemplateBuilder to avoid
     * dependency issues in test context. It provides basic HTTP client
     * functionality for test scenarios.
     *
     * @return Simple RestTemplate instance for testing
     */
    @Bean
    @Primary
    fun testRestTemplate(): RestTemplate {
        return RestTemplate()
    }
} 