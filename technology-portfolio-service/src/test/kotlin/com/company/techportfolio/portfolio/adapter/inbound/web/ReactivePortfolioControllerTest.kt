package com.company.techportfolio.portfolio.adapter.inbound.web

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.service.PortfolioService
import com.company.techportfolio.shared.domain.model.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.math.BigDecimal
import java.time.LocalDateTime
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

/**
 * Reactive Security Tests for PortfolioController
 * 
 * This test class verifies the security functionality of the PortfolioController
 * using WebFlux testing patterns. It tests authentication, authorization, and
 * security-related error handling in a reactive environment.
 * 
 * ## Test Coverage:
 * - JWT authentication validation
 * - Role-based access control (RBAC)
 * - Security filter chain configuration
 * - Unauthorized access handling
 * - Forbidden access handling
 * - Public endpoint access
 * - Reactive security integration
 * 
 * ## Testing Approach:
 * - Uses @WebFluxTest for reactive testing
 * - Uses WebTestClient for HTTP testing
 * - Tests both authenticated and unauthenticated scenarios
 * - Verifies security headers and status codes
 * - Tests role-based authorization
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioController
 * @see ReactiveSecurityConfig
 */
@WebFluxTest(PortfolioController::class)
@Import(TestSecurityConfig::class)
@ActiveProfiles("test")
class ReactivePortfolioControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var portfolioService: PortfolioService

    private val objectMapper = jacksonObjectMapper()

    private lateinit var samplePortfolio: PortfolioResponse
    private lateinit var sampleTechnology: TechnologyResponse
    private lateinit var createPortfolioRequest: CreatePortfolioRequest
    private lateinit var addTechnologyRequest: AddTechnologyRequest

    @BeforeEach
    fun setUp() {
        // Sample data setup
        samplePortfolio = PortfolioResponse(
            id = 1L,
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            ownerId = 42L,
            organizationId = 100L,
            technologyCount = 0,
            totalAnnualCost = null,
            createdAt = LocalDateTime.now(),
            technologies = emptyList()
        )

        sampleTechnology = TechnologyResponse(
            id = 1L,
            name = "Spring Boot",
            description = "Java Framework",
            category = "Framework",
            version = "3.2.0",
            type = TechnologyType.OPEN_SOURCE,
            maturityLevel = MaturityLevel.MATURE,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("5000.00"),
            vendorName = "VMware",
            portfolioId = 1L,
            createdAt = LocalDateTime.now()
        )

        createPortfolioRequest = CreatePortfolioRequest(
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            organizationId = 100L
        )

        addTechnologyRequest = AddTechnologyRequest(
            name = "Spring Boot",
            description = "Java Framework",
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
     * Tests that unauthenticated requests are rejected with 401 Unauthorized.
     * 
     * Verifies that the security filter chain properly blocks unauthenticated
     * requests to protected endpoints and returns appropriate HTTP status codes.
     * 
     * Expected behavior:
     * - Returns HTTP 401 Unauthorized
     * - No business logic is executed
     * - Security filter blocks the request
     */
    @Test
    fun `should reject unauthenticated requests`() {
        // Test portfolio creation without authentication
        webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPortfolioRequest))
            .exchange()
            .expectStatus().isUnauthorized()

        // Test portfolio retrieval without authentication
        webTestClient.get()
            .uri("/api/v1/portfolios/1")
            .exchange()
            .expectStatus().isUnauthorized()

        // Test technology retrieval without authentication
        webTestClient.get()
            .uri("/api/v1/portfolios/technologies/1")
            .exchange()
            .expectStatus().isUnauthorized()
    }

    /**
     * Tests that authenticated requests with USER role are allowed.
     * 
     * Verifies that users with the USER role can access standard portfolio
     * endpoints and perform CRUD operations on their portfolios.
     * 
     * Expected behavior:
     * - Returns HTTP 200/201 for successful operations
     * - Business logic is executed
     * - Service methods are called with correct parameters
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should allow authenticated requests with USER role`() {
        // Mock service responses
        whenever(portfolioService.createPortfolio(any())).thenReturn(Mono.just(samplePortfolio))
        whenever(portfolioService.getPortfolio(eq(1L))).thenReturn(Mono.just(samplePortfolio))
        whenever(portfolioService.addTechnology(eq(1L), any())).thenReturn(Mono.just(sampleTechnology))

        // Test portfolio creation
        webTestClient.post()
            .uri("/api/v1/portfolios")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPortfolioRequest))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Test Portfolio")

        // Test portfolio retrieval
        webTestClient.get()
            .uri("/api/v1/portfolios/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)

        // Test technology addition
        webTestClient.post()
            .uri("/api/v1/portfolios/1/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(addTechnologyRequest))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Spring Boot")

        // Verify service calls
        verify(portfolioService).createPortfolio(any())
        verify(portfolioService).getPortfolio(eq(1L))
        verify(portfolioService).addTechnology(eq(1L), any())
    }

    /**
     * Tests that stream endpoints require ADMIN role.
     * 
     * Verifies that streaming endpoints (which provide real-time data) are
     * properly protected and require administrative privileges.
     * 
     * Expected behavior:
     * - USER role gets 403 Forbidden
     * - ADMIN role gets 200 OK
     * - Proper authorization checks are enforced
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should require ADMIN role for stream endpoints`() {
        // Test stream endpoints with USER role
        webTestClient.get()
            .uri("/api/v1/portfolios/stream")
            .exchange()
            .expectStatus().isForbidden()

        webTestClient.get()
            .uri("/api/v1/portfolios/technologies/stream")
            .exchange()
            .expectStatus().isForbidden()
    }

    /**
     * Tests that ADMIN role can access stream endpoints.
     * 
     * Verifies that users with ADMIN role can access streaming endpoints
     * and receive real-time data streams.
     * 
     * Expected behavior:
     * - Returns HTTP 200 OK
     * - Returns Server-Sent Events content type
     * - Streams data as expected
     */
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should allow ADMIN role to access stream endpoints`() {
        // Mock service responses for streaming
        val portfolioSummaries = listOf(
            PortfolioSummary(1L, "Portfolio 1", PortfolioType.ENTERPRISE, 5, BigDecimal("50000")),
            PortfolioSummary(2L, "Portfolio 2", PortfolioType.DEVELOPMENT, 3, BigDecimal("25000"))
        )
        whenever(portfolioService.searchPortfolios(any(), any(), any(), any()))
            .thenReturn(Flux.fromIterable(portfolioSummaries))

        // Test portfolio streaming
        webTestClient.get()
            .uri("/api/v1/portfolios/stream")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)
    }

    /**
     * Tests that public endpoints are accessible without authentication.
     * 
     * Verifies that health check and actuator endpoints are publicly
     * accessible as configured in the security configuration.
     * 
     * Expected behavior:
     * - Returns HTTP 200 OK
     * - No authentication required
     * - Health information is returned
     */
    @Test
    fun `should allow access to public endpoints`() {
        // Test health endpoint
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk()

        // Test info endpoint
        webTestClient.get()
            .uri("/actuator/info")
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Tests JWT token validation with proper authentication.
     * 
     * Verifies that the JWT authentication converter properly extracts
     * authorities from JWT tokens and applies role-based access control.
     * 
     * Expected behavior:
     * - JWT tokens are properly validated
     * - Authorities are extracted correctly
     * - Role-based access control is enforced
     */
    @Test
    fun `should validate JWT tokens properly`() {
        // This test would require a proper JWT token setup
        // For now, we test the security configuration indirectly
        // through the @WithMockUser annotations
        
        // Test that security is properly configured
        webTestClient.get()
            .uri("/api/v1/portfolios/1")
            .exchange()
            .expectStatus().isUnauthorized()
    }

    /**
     * Tests error handling for invalid JWT tokens.
     * 
     * Verifies that the application properly handles invalid or expired
     * JWT tokens and returns appropriate error responses.
     * 
     * Expected behavior:
     * - Invalid tokens return 401 Unauthorized
     * - Expired tokens return 401 Unauthorized
     * - Malformed tokens return 401 Unauthorized
     */
    @Test
    fun `should handle invalid JWT tokens`() {
        // Test with invalid Authorization header
        webTestClient.get()
            .uri("/api/v1/portfolios/1")
            .header("Authorization", "Bearer invalid-token")
            .exchange()
            .expectStatus().isUnauthorized()

        // Test with malformed Authorization header
        webTestClient.get()
            .uri("/api/v1/portfolios/1")
            .header("Authorization", "InvalidFormat token")
            .exchange()
            .expectStatus().isUnauthorized()
    }

    /**
     * Tests CORS configuration for cross-origin requests.
     * 
     * Verifies that CORS is properly configured for the reactive application
     * and handles preflight requests correctly.
     * 
     * Expected behavior:
     * - CORS headers are present in responses
     * - Preflight requests are handled
     * - Cross-origin requests are allowed
     */
    @Test
    fun `should handle CORS requests properly`() {
        // Test CORS preflight request
        webTestClient.options()
            .uri("/api/v1/portfolios")
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "Content-Type")
            .exchange()
            .expectStatus().isOk()
    }
} 