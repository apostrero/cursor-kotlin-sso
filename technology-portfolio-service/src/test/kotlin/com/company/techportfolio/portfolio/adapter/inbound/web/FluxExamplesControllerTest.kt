package com.company.techportfolio.portfolio.adapter.inbound.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier
import reactor.core.publisher.Flux
import org.springframework.http.MediaType

/**
 * Test class for FluxExamplesController demonstrating various testing approaches
 * for Flux<T> endpoints in Spring WebFlux.
 * 
 * This test class showcases:
 * - WebTestClient testing for HTTP endpoints
 * - StepVerifier testing for reactive streams
 * - Testing different Flux patterns and operators
 * - Error handling testing
 * - Server-Sent Events testing
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@WebFluxTest(FluxExamplesController::class)
class FluxExamplesControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var fluxExamplesController: FluxExamplesController

    /**
     * Test basic Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing a simple Flux endpoint that returns
     * a stream of numbers.
     */
    @Test
    fun `test basic flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/basic")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Long::class.java)
            .hasSize(10)
            .contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
    }

    /**
     * Test portfolio Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing a Flux endpoint that returns
     * custom objects (PortfolioSummary).
     */
    @Test
    fun `test portfolio flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/portfolios")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(5)
    }

    /**
     * Test filtered Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing a Flux endpoint with query parameters
     * and filtering logic.
     */
    @Test
    fun `test filtered portfolio flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/portfolios/filtered?minCost=3000")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(3) // Only portfolios with cost >= 3000
    }

    /**
     * Test transformed Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing a Flux endpoint that transforms
     * data using reactive operators.
     */
    @Test
    fun `test transformed portfolio flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/portfolios/transformed")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(5)
            .allSatisfy { item ->
                assert(item.contains("Portfolio"))
                assert(item.contains("technologies"))
            }
    }

    /**
     * Test error handling Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing error scenarios in Flux streams.
     */
    @Test
    fun `test error handling flux endpoint - success case`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/error-handling?shouldError=false")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(5)
            .contains("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
    }

    /**
     * Test error handling Flux endpoint with error simulation.
     * 
     * Demonstrates testing error recovery in Flux streams.
     */
    @Test
    fun `test error handling flux endpoint - error case`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/error-handling?shouldError=true")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(3) // Error occurs at item 3, recovery happens
    }

    /**
     * Test Server-Sent Events (SSE) endpoint using WebTestClient.
     * 
     * Demonstrates testing SSE streaming endpoints.
     */
    @Test
    fun `test sse flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/sse")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)
            .expectBodyList(String::class.java)
            .hasSize(10)
    }

    /**
     * Test backpressure Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing endpoints with backpressure handling.
     */
    @Test
    fun `test backpressure flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/backpressure")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Long::class.java)
            .hasSize(1000)
    }

    /**
     * Test combined Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing endpoints that combine multiple Flux streams.
     */
    @Test
    fun `test combined flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/combined")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(6) // A, B, C, 1, 2, 3
            .contains("Combined: A", "Combined: B", "Combined: C", "Combined: 1", "Combined: 2", "Combined: 3")
    }

    /**
     * Test conditional Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing endpoints with conditional logic.
     */
    @Test
    fun `test conditional flux endpoint - portfolios`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/conditional?type=portfolios")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(5)
            .allSatisfy { item ->
                assert(item.startsWith("Portfolio:"))
            }
    }

    /**
     * Test conditional Flux endpoint with different type.
     * 
     * Demonstrates testing different conditional branches.
     */
    @Test
    fun `test conditional flux endpoint - technologies`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/conditional?type=technologies")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(5)
            .allSatisfy { item ->
                assert(item.startsWith("Technology:"))
            }
    }

    /**
     * Test paginated Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing pagination with Flux streams.
     */
    @Test
    fun `test paged flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/paged?page=0&size=2")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(2)
    }

    /**
     * Test cached Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing caching behavior in Flux streams.
     */
    @Test
    fun `test cached flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/cached")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(5)
            .contains("Cached item 1", "Cached item 2", "Cached item 3", "Cached item 4", "Cached item 5")
    }

    /**
     * Test retry Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing retry logic in Flux streams.
     */
    @Test
    fun `test retry flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/retry")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(3)
    }

    /**
     * Test error mapping Flux endpoint using WebTestClient.
     * 
     * Demonstrates testing error transformation in Flux streams.
     */
    @Test
    fun `test error mapping flux endpoint`() {
        webTestClient.get()
            .uri("/api/v1/flux-examples/error-mapping")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(3)
    }

    // StepVerifier Tests - Direct Controller Testing

    /**
     * Test basic Flux using StepVerifier.
     * 
     * Demonstrates testing Flux streams directly using StepVerifier
     * for more granular control over reactive stream testing.
     */
    @Test
    fun `test basic flux with step verifier`() {
        val flux = fluxExamplesController.basicFlux()
        
        StepVerifier.create(flux)
            .expectNext(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
            .verifyComplete()
    }

    /**
     * Test portfolio Flux using StepVerifier.
     * 
     * Demonstrates testing Flux streams with custom objects.
     */
    @Test
    fun `test portfolio flux with step verifier`() {
        val flux = fluxExamplesController.portfolioFlux()
        
        StepVerifier.create(flux)
            .expectNextCount(5)
            .verifyComplete()
    }

    /**
     * Test filtered Flux using StepVerifier.
     * 
     * Demonstrates testing filtered Flux streams.
     */
    @Test
    fun `test filtered portfolio flux with step verifier`() {
        val flux = fluxExamplesController.filteredPortfolioFlux(3000.0)
        
        StepVerifier.create(flux)
            .expectNextCount(3) // Only portfolios with cost >= 3000
            .verifyComplete()
    }

    /**
     * Test transformed Flux using StepVerifier.
     * 
     * Demonstrates testing transformed Flux streams.
     */
    @Test
    fun `test transformed portfolio flux with step verifier`() {
        val flux = fluxExamplesController.transformedPortfolioFlux()
        
        StepVerifier.create(flux)
            .expectNextCount(5)
            .allSatisfy { item ->
                assert(item.contains("Portfolio"))
                assert(item.contains("technologies"))
            }
            .verifyComplete()
    }

    /**
     * Test error handling Flux using StepVerifier.
     * 
     * Demonstrates testing error scenarios with StepVerifier.
     */
    @Test
    fun `test error handling flux with step verifier - success`() {
        val flux = fluxExamplesController.errorHandlingFlux(false)
        
        StepVerifier.create(flux)
            .expectNext("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
            .verifyComplete()
    }

    /**
     * Test error handling Flux with error simulation using StepVerifier.
     * 
     * Demonstrates testing error recovery with StepVerifier.
     */
    @Test
    fun `test error handling flux with step verifier - error`() {
        val flux = fluxExamplesController.errorHandlingFlux(true)
        
        StepVerifier.create(flux)
            .expectNext("Item 1", "Item 2")
            .expectNext("Error recovered: Simulated error at item 3")
            .verifyComplete()
    }

    /**
     * Test SSE Flux using StepVerifier.
     * 
     * Demonstrates testing SSE streams with StepVerifier.
     */
    @Test
    fun `test sse flux with step verifier`() {
        val flux = fluxExamplesController.sseFlux()
        
        StepVerifier.create(flux)
            .expectNextCount(10)
            .verifyComplete()
    }

    /**
     * Test backpressure Flux using StepVerifier.
     * 
     * Demonstrates testing backpressure handling with StepVerifier.
     */
    @Test
    fun `test backpressure flux with step verifier`() {
        val flux = fluxExamplesController.backpressureFlux()
        
        StepVerifier.create(flux)
            .expectNextCount(1000)
            .verifyComplete()
    }

    /**
     * Test combined Flux using StepVerifier.
     * 
     * Demonstrates testing combined Flux streams with StepVerifier.
     */
    @Test
    fun `test combined flux with step verifier`() {
        val flux = fluxExamplesController.combinedFlux()
        
        StepVerifier.create(flux)
            .expectNext("Combined: A", "Combined: B", "Combined: C", "Combined: 1", "Combined: 2", "Combined: 3")
            .verifyComplete()
    }

    /**
     * Test conditional Flux using StepVerifier.
     * 
     * Demonstrates testing conditional Flux streams with StepVerifier.
     */
    @Test
    fun `test conditional flux with step verifier - portfolios`() {
        val flux = fluxExamplesController.conditionalFlux("portfolios")
        
        StepVerifier.create(flux)
            .expectNextCount(5)
            .allSatisfy { item ->
                assert(item.startsWith("Portfolio:"))
            }
            .verifyComplete()
    }

    /**
     * Test paginated Flux using StepVerifier.
     * 
     * Demonstrates testing pagination with StepVerifier.
     */
    @Test
    fun `test paged flux with step verifier`() {
        val flux = fluxExamplesController.pagedFlux(0, 2)
        
        StepVerifier.create(flux)
            .expectNextCount(2)
            .verifyComplete()
    }

    /**
     * Test cached Flux using StepVerifier.
     * 
     * Demonstrates testing caching behavior with StepVerifier.
     */
    @Test
    fun `test cached flux with step verifier`() {
        val flux = fluxExamplesController.cachedFlux()
        
        StepVerifier.create(flux)
            .expectNext("Cached item 1", "Cached item 2", "Cached item 3", "Cached item 4", "Cached item 5")
            .verifyComplete()
    }

    /**
     * Test retry Flux using StepVerifier.
     * 
     * Demonstrates testing retry logic with StepVerifier.
     */
    @Test
    fun `test retry flux with step verifier`() {
        val flux = fluxExamplesController.retryFlux()
        
        StepVerifier.create(flux)
            .expectNextCount(3)
            .verifyComplete()
    }

    /**
     * Test error mapping Flux using StepVerifier.
     * 
     * Demonstrates testing error transformation with StepVerifier.
     */
    @Test
    fun `test error mapping flux with step verifier`() {
        val flux = fluxExamplesController.errorMappingFlux()
        
        StepVerifier.create(flux)
            .expectNextCount(3)
            .verifyComplete()
    }
} 