package com.company.techportfolio.gateway

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ApiGatewayApplicationTest {

    @Test
    fun `should load application context`() {
        // This test verifies that the Spring Boot application context loads successfully
        // If the context fails to load, the test will fail
    }

    @Test
    fun `should have main method for application startup`() {
        // Verify the main method exists and can be called
        // This is important for Spring Boot applications
        val mainMethod = ApiGatewayApplication::class.java.getDeclaredMethod("main", Array<String>::class.java)
        assert(mainMethod != null)
        assert(mainMethod.parameterTypes.size == 1)
        assert(mainMethod.parameterTypes[0] == Array<String>::class.java)
    }
} 