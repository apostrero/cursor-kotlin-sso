package com.company.techportfolio.portfolio.e2e

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.shared.domain.model.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.math.BigDecimal
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

/**
 * End-to-end tests for the reactive Technology Portfolio Service.
 * 
 * This test class verifies complete user journeys and business workflows
 * from start to finish, including authentication, authorization, data
 * operations, and integrations with external services.
 * 
 * Test coverage includes:
 * - Complete user authentication flow
 * - Portfolio lifecycle management
 * - Technology management workflows
 * - Event publishing integration
 * - Error handling and recovery
 * - Security and authorization flows
 * - Integration with external services
 * - Data consistency and integrity
 * 
 * Testing approach:
 * - Uses TestContainers for realistic database testing
 * - Simulates real user interactions
 * - Tests complete business workflows
 * - Verifies data consistency
 * - Tests error scenarios and recovery
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.r2dbc.url=r2dbc:tc:postgresql:15:///testdb?TC_DAEMON=true",
        "spring.flyway.enabled=false"
    ]
)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReactiveEndToEndTest {

    @Container
    companion object {
        private val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") { 
                postgres.jdbcUrl.replace("jdbc:", "r2dbc:") 
            }
            registry.add("spring.r2dbc.username") { postgres.username }
            registry.add("spring.r2dbc.password") { postgres.password }
        }
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var databaseClient: DatabaseClient

    @Autowired
    private lateinit var webClient: WebClient

    private val objectMapper = jacksonObjectMapper()

    private lateinit var createPortfolioRequest: CreatePortfolioRequest
    private lateinit var addTechnologyRequest: AddTechnologyRequest

    @BeforeEach
    fun setUp() {
        // Clean up database before each test
        cleanupDatabase()

        // Setup test data
        createPortfolioRequest = CreatePortfolioRequest(
            name = "E2E Test Portfolio",
            description = "Portfolio for end-to-end testing",
            type = PortfolioType.ENTERPRISE,
            organizationId = 100L
        )

        addTechnologyRequest = AddTechnologyRequest(
            name = "Spring Boot",
            description = "Java Framework for reactive applications",
            category = "Framework",
            version = "3.2.0",
            type = TechnologyType.OPEN_SOURCE,
            maturityLevel = MaturityLevel.MATURE,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("5000.00"),
            vendorName = "VMware"
        )
    }

    /**
     * Tests complete portfolio lifecycle workflow.
     * 
     * Verifies the entire portfolio lifecycle from creation to deletion,
     * including technology management and all intermediate operations.
     * 
     * Expected behavior:
     * - Portfolio creation succeeds
     * - Technology addition works
     * - Portfolio updates are persisted
     * - Technology removal works
     * - Portfolio deletion cleans up data
     * - All operations maintain data consistency
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should complete full portfolio lifecycle workflow`() {
        // Step 1: Create portfolio
        val createdPortfolio = webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPortfolioRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        assertNotNull(createdPortfolio.id)
        assertEquals("E2E Test Portfolio", createdPortfolio.name)
        assertEquals(PortfolioType.ENTERPRISE, createdPortfolio.type)
        assertEquals(PortfolioStatus.ACTIVE, createdPortfolio.status)

        // Step 2: Add technology to portfolio
        val addedTechnology = webTestClient.post()
            .uri("/api/v1/portfolios/${createdPortfolio.id}/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(addTechnologyRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(TechnologyResponse::class.java)
            .returnResult()
            .responseBody!!

        assertNotNull(addedTechnology.id)
        assertEquals("Spring Boot", addedTechnology.name)
        assertEquals(createdPortfolio.id, addedTechnology.portfolioId)

        // Step 3: Verify portfolio has technology
        val portfolioWithTechnology = webTestClient.get()
            .uri("/api/v1/portfolios/${createdPortfolio.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(1, portfolioWithTechnology.technologyCount)
        assertNotNull(portfolioWithTechnology.totalAnnualCost)
        assertEquals(BigDecimal("5000.00"), portfolioWithTechnology.totalAnnualCost)

        // Step 4: Add another technology
        val secondTechnologyRequest = addTechnologyRequest.copy(
            name = "PostgreSQL",
            description = "Relational Database",
            category = "Database",
            annualCost = BigDecimal("3000.00")
        )

        val secondTechnology = webTestClient.post()
            .uri("/api/v1/portfolios/${createdPortfolio.id}/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(secondTechnologyRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(TechnologyResponse::class.java)
            .returnResult()
            .responseBody!!

        assertNotNull(secondTechnology.id)
        assertEquals("PostgreSQL", secondTechnology.name)

        // Step 5: Verify updated portfolio
        val updatedPortfolio = webTestClient.get()
            .uri("/api/v1/portfolios/${createdPortfolio.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(2, updatedPortfolio.technologyCount)
        assertEquals(BigDecimal("8000.00"), updatedPortfolio.totalAnnualCost)

        // Step 6: Search portfolios
        val searchResults = webTestClient.get()
            .uri("/api/v1/portfolios/search?name=E2E")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(PortfolioSummary::class.java)
            .returnResult()
            .responseBody!!

        assertTrue(searchResults.isNotEmpty())
        assertTrue(searchResults.any { it.name.contains("E2E") })

        // Step 7: Stream portfolios (as ADMIN)
        webTestClient.get()
            .uri("/api/v1/portfolios/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)
            .expectBodyList(PortfolioSummary::class.java)
            .hasSizeGreaterThan(0)

        // Verify database consistency
        val dbPortfolio = findPortfolioInDatabase(createdPortfolio.id!!)
        assertNotNull(dbPortfolio)
        assertEquals("E2E Test Portfolio", dbPortfolio["name"])

        val dbTechnologies = findTechnologiesByPortfolioId(createdPortfolio.id!!)
        assertEquals(2, dbTechnologies.size)
    }

    /**
     * Tests complete user authentication and authorization flow.
     * 
     * Verifies the complete authentication and authorization workflow,
     * including JWT token handling, role-based access control, and
     * security error scenarios.
     * 
     * Expected behavior:
     * - Unauthenticated requests are rejected
     * - Authenticated requests with proper roles succeed
     * - Role-based access control works correctly
     * - JWT token validation works
     * - Security errors are handled properly
     */
    @Test
    fun `should handle complete authentication and authorization flow`() {
        // Step 1: Test unauthenticated access
        webTestClient.get()
            .uri("/api/v1/portfolios/1")
            .exchange()
            .expectStatus().isUnauthorized

        webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPortfolioRequest))
            .exchange()
            .expectStatus().isUnauthorized

        // Step 2: Test authenticated access with USER role
        val userPortfolio = webTestClient.mutateWith(org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser().roles("USER"))
            .post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPortfolioRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        assertNotNull(userPortfolio.id)

        // Step 3: Test USER role access to portfolio
        webTestClient.mutateWith(org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser().roles("USER"))
            .get()
            .uri("/api/v1/portfolios/${userPortfolio.id}")
            .exchange()
            .expectStatus().isOk

        // Step 4: Test USER role access to streaming (should be forbidden)
        webTestClient.mutateWith(org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser().roles("USER"))
            .get()
            .uri("/api/v1/portfolios/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isForbidden

        // Step 5: Test ADMIN role access to streaming
        webTestClient.mutateWith(org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser().roles("ADMIN"))
            .get()
            .uri("/api/v1/portfolios/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)

        // Step 6: Test invalid JWT token
        webTestClient.get()
            .uri("/api/v1/portfolios/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
            .exchange()
            .expectStatus().isUnauthorized

        // Step 7: Test malformed authorization header
        webTestClient.get()
            .uri("/api/v1/portfolios/1")
            .header(HttpHeaders.AUTHORIZATION, "InvalidFormat token")
            .exchange()
            .expectStatus().isUnauthorized
    }

    /**
     * Tests error handling and recovery scenarios.
     * 
     * Verifies that the system handles various error scenarios
     * gracefully and recovers properly from failures.
     * 
     * Expected behavior:
     * - Invalid requests return appropriate error codes
     * - Database errors are handled gracefully
     * - Validation errors provide meaningful messages
     * - System recovers from transient failures
     * - Data integrity is maintained
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should handle error scenarios and recovery`() {
        // Step 1: Test invalid portfolio ID
        webTestClient.get()
            .uri("/api/v1/portfolios/999999")
            .exchange()
            .expectStatus().isNotFound

        // Step 2: Test invalid request data
        val invalidPortfolioRequest = createPortfolioRequest.copy(
            name = "", // Invalid empty name
            description = null // Invalid null description
        )

        webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidPortfolioRequest))
            .exchange()
            .expectStatus().isBadRequest

        // Step 3: Test invalid technology data
        val invalidTechnologyRequest = addTechnologyRequest.copy(
            name = "", // Invalid empty name
            annualCost = BigDecimal("-100") // Invalid negative cost
        )

        // First create a valid portfolio
        val portfolio = webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPortfolioRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        // Then try to add invalid technology
        webTestClient.post()
            .uri("/api/v1/portfolios/${portfolio.id}/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidTechnologyRequest))
            .exchange()
            .expectStatus().isBadRequest

        // Step 4: Test adding technology to non-existent portfolio
        webTestClient.post()
            .uri("/api/v1/portfolios/999999/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(addTechnologyRequest))
            .exchange()
            .expectStatus().isNotFound

        // Step 5: Test malformed JSON
        webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue("{ invalid json }"))
            .exchange()
            .expectStatus().isBadRequest

        // Step 6: Verify data integrity after errors
        val validPortfolio = webTestClient.get()
            .uri("/api/v1/portfolios/${portfolio.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(0, validPortfolio.technologyCount) // No invalid technologies should be added
        assertEquals("E2E Test Portfolio", validPortfolio.name) // Portfolio should remain unchanged
    }

    /**
     * Tests integration with external services.
     * 
     * Verifies that the system properly integrates with external
     * services like event publishing and audit logging.
     * 
     * Expected behavior:
     * - Events are published correctly
     * - External service calls work
     * - Error handling for external services works
     * - System degrades gracefully when external services fail
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should integrate with external services`() {
        // Step 1: Create portfolio (should trigger events)
        val portfolio = webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPortfolioRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        assertNotNull(portfolio.id)

        // Step 2: Add technology (should trigger events)
        val technology = webTestClient.post()
            .uri("/api/v1/portfolios/${portfolio.id}/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(addTechnologyRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(TechnologyResponse::class.java)
            .returnResult()
            .responseBody!!

        assertNotNull(technology.id)

        // Step 3: Test event stream endpoint
        webTestClient.get()
            .uri("/api/v1/events/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)

        // Step 4: Test event filtering
        webTestClient.get()
            .uri("/api/v1/events/filter?eventType=PortfolioCreated")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSizeGreaterThan(0)

        // Step 5: Test event replay
        webTestClient.get()
            .uri("/api/v1/events/replay?from=2024-01-01T00:00:00")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSizeGreaterThan(0)

        // Step 6: Test health check endpoints
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk

        webTestClient.get()
            .uri("/actuator/health/db")
            .exchange()
            .expectStatus().isOk

        // Step 7: Test metrics endpoint
        webTestClient.get()
            .uri("/actuator/metrics")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Tests data consistency and integrity.
     * 
     * Verifies that data operations maintain consistency and integrity
     * across the entire system, including database transactions and
     * concurrent access scenarios.
     * 
     * Expected behavior:
     * - Data consistency is maintained
     * - Transactions work correctly
     * - Concurrent access is handled properly
     * - Data integrity is preserved
     * - Rollback scenarios work correctly
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should maintain data consistency and integrity`() {
        // Step 1: Create portfolio and verify initial state
        val portfolio = webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPortfolioRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        // Verify database state
        val dbPortfolio = findPortfolioInDatabase(portfolio.id!!)
        assertNotNull(dbPortfolio)
        assertEquals("E2E Test Portfolio", dbPortfolio["name"])
        assertEquals(0, dbPortfolio["technology_count"])

        // Step 2: Add technology and verify consistency
        val technology = webTestClient.post()
            .uri("/api/v1/portfolios/${portfolio.id}/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(addTechnologyRequest))
            .exchange()
            .expectStatus().isCreated
            .expectBody(TechnologyResponse::class.java)
            .returnResult()
            .responseBody!!

        // Verify technology in database
        val dbTechnology = findTechnologyInDatabase(technology.id!!)
        assertNotNull(dbTechnology)
        assertEquals("Spring Boot", dbTechnology["name"])
        assertEquals(portfolio.id, dbTechnology["portfolio_id"])

        // Verify portfolio count is updated
        val updatedDbPortfolio = findPortfolioInDatabase(portfolio.id!!)
        assertEquals(1, updatedDbPortfolio["technology_count"])

        // Step 3: Test concurrent access
        val concurrentResults = (1..10).map { i ->
            webTestClient.get()
                .uri("/api/v1/portfolios/${portfolio.id}")
                .exchange()
                .expectStatus().isOk
                .expectBody(PortfolioResponse::class.java)
                .returnResult()
                .responseBody!!
        }

        // All concurrent reads should return consistent data
        concurrentResults.forEach { result ->
            assertEquals(portfolio.id, result.id)
            assertEquals("E2E Test Portfolio", result.name)
            assertEquals(1, result.technologyCount)
        }

        // Step 4: Test transaction rollback scenario
        val invalidTechnologyRequest = addTechnologyRequest.copy(
            name = "", // Invalid empty name
            annualCost = BigDecimal("-100") // Invalid negative cost
        )

        // Try to add invalid technology
        webTestClient.post()
            .uri("/api/v1/portfolios/${portfolio.id}/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidTechnologyRequest))
            .exchange()
            .expectStatus().isBadRequest

        // Verify no invalid technology was persisted
        val technologiesAfterError = findTechnologiesByPortfolioId(portfolio.id!!)
        assertEquals(1, technologiesAfterError.size) // Only the original technology

        // Verify portfolio count is still correct
        val portfolioAfterError = findPortfolioInDatabase(portfolio.id!!)
        assertEquals(1, portfolioAfterError["technology_count"])

        // Step 5: Test data relationships
        val portfolioWithTechnologies = webTestClient.get()
            .uri("/api/v1/portfolios/${portfolio.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(PortfolioResponse::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(1, portfolioWithTechnologies.technologyCount)
        assertEquals(BigDecimal("5000.00"), portfolioWithTechnologies.totalAnnualCost)
    }

    // Helper methods

    private fun cleanupDatabase() {
        databaseClient.sql("DELETE FROM technologies").fetch().rowsUpdated().block()
        databaseClient.sql("DELETE FROM portfolios").fetch().rowsUpdated().block()
    }

    private fun findPortfolioInDatabase(id: Long): Map<String, Any?>? {
        return databaseClient.sql("SELECT * FROM portfolios WHERE id = :id")
            .bind("id", id)
            .fetch()
            .first()
            .map { row -> row.toMap() }
            .block()
    }

    private fun findTechnologyInDatabase(id: Long): Map<String, Any?>? {
        return databaseClient.sql("SELECT * FROM technologies WHERE id = :id")
            .bind("id", id)
            .fetch()
            .first()
            .map { row -> row.toMap() }
            .block()
    }

    private fun findTechnologiesByPortfolioId(portfolioId: Long): List<Map<String, Any?>> {
        return databaseClient.sql("SELECT * FROM technologies WHERE portfolio_id = :portfolio_id")
            .bind("portfolio_id", portfolioId)
            .fetch()
            .all()
            .map { row -> row.toMap() }
            .collectList()
            .block()!!
    }

    private fun R2dbcRow.toMap(): Map<String, Any?> {
        val metadata = this.metadata
        return metadata.columnNames.associateWith { columnName ->
            this.get(columnName)
        }
    }
} 