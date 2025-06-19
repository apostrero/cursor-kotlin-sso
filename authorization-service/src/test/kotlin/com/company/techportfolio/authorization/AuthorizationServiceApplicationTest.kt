package com.company.techportfolio.authorization

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class AuthorizationServiceApplicationTest {

    @Test
    fun `should load application context successfully`() {
        // This test will pass if the Spring Boot application context loads without errors
        // It tests that all beans can be created and dependencies are satisfied
    }
} 