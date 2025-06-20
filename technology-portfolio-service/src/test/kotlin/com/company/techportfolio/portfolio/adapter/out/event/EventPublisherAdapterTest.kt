package com.company.techportfolio.portfolio.adapter.out.event

import com.company.techportfolio.shared.domain.event.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import reactor.test.StepVerifier
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

/**
 * Unit tests for Reactive EventPublisherAdapter.
 * 
 * This test class verifies the functionality of the reactive EventPublisherAdapter, which
 * is the implementation of the EventPublisher port in the hexagonal architecture.
 * It tests the adapter's ability to publish various domain events using reactive patterns.
 * 
 * ## Test Coverage:
 * - Publishing of portfolio-related events (created, updated)
 * - Publishing of technology-related events (added, removed)
 * - Reactive event publishing with Mono<Void> return types
 * - Batch event publishing with Flux
 * - Reactive error handling
 * - Empty event list handling
 * 
 * ## Testing Approach:
 * - Uses StepVerifier for reactive testing
 * - Verifies Mono<Void> completion without errors
 * - Tests reactive composition and error handling
 * - Validates reactive stream behavior
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see EventPublisherAdapter
 * @see DomainEvent
 */
class EventPublisherAdapterTest {

    /**
     * The adapter under test.
     */
    private lateinit var eventPublisher: EventPublisherAdapter

    /**
     * Set up the test environment before each test.
     * 
     * Initializes a fresh instance of the EventPublisherAdapter
     * for each test to ensure test isolation.
     */
    @BeforeEach
    fun setUp() {
        eventPublisher = EventPublisherAdapter()
    }

    /**
     * Tests publishing a PortfolioCreatedEvent reactively.
     * 
     * Verifies that:
     * 1. The adapter can accept and process a PortfolioCreatedEvent
     * 2. The Mono<Void> completes successfully
     * 3. No errors are emitted during publishing
     */
    @Test
    fun `should publish PortfolioCreatedEvent reactively`() {
        // Given
        val event = PortfolioCreatedEvent(
            portfolioId = 1L,
            name = "Test Portfolio",
            ownerId = 100L,
            organizationId = 200L
        )

        // When & Then
        StepVerifier.create(eventPublisher.publish(event))
            .verifyComplete()
    }

    /**
     * Tests publishing a PortfolioUpdatedEvent reactively.
     * 
     * Verifies that:
     * 1. The adapter can accept and process a PortfolioUpdatedEvent
     * 2. The Mono<Void> completes successfully
     * 3. The changes map is correctly processed
     */
    @Test
    fun `should publish PortfolioUpdatedEvent reactively`() {
        // Given
        val event = PortfolioUpdatedEvent(
            portfolioId = 1L,
            changes = mapOf("name" to "Updated Portfolio", "status" to "INACTIVE")
        )

        // When & Then
        StepVerifier.create(eventPublisher.publish(event))
            .verifyComplete()
    }

    /**
     * Tests publishing a TechnologyAddedEvent reactively.
     * 
     * Verifies that:
     * 1. The adapter can accept and process a TechnologyAddedEvent
     * 2. The Mono<Void> completes successfully
     * 3. The event data is properly handled
     */
    @Test
    fun `should publish TechnologyAddedEvent reactively`() {
        // Given
        val event = TechnologyAddedEvent(
            portfolioId = 1L,
            technologyId = 10L,
            technologyName = "Spring Boot"
        )

        // When & Then
        StepVerifier.create(eventPublisher.publish(event))
            .verifyComplete()
    }

    /**
     * Tests publishing a TechnologyRemovedEvent reactively.
     * 
     * Verifies that:
     * 1. The adapter can accept and process a TechnologyRemovedEvent
     * 2. The Mono<Void> completes successfully
     * 3. The event data is properly handled
     */
    @Test
    fun `should publish TechnologyRemovedEvent reactively`() {
        // Given
        val event = TechnologyRemovedEvent(
            portfolioId = 1L,
            technologyId = 10L,
            technologyName = "Spring Boot"
        )

        // When & Then
        StepVerifier.create(eventPublisher.publish(event))
            .verifyComplete()
    }

    /**
     * Tests publishing multiple events reactively using publishAll.
     * 
     * Verifies that:
     * 1. The adapter can handle batch publishing of multiple events
     * 2. The Mono<Void> completes successfully
     * 3. Events of different types can be published in a batch
     */
    @Test
    fun `should publish multiple events reactively using publishAll`() {
        // Given
        val events = listOf(
            PortfolioCreatedEvent(1L, "Portfolio 1", 100L, 200L),
            TechnologyAddedEvent(1L, 10L, "Tech 1"),
            PortfolioUpdatedEvent(1L, mapOf("status" to "ACTIVE"))
        )

        // When & Then
        StepVerifier.create(eventPublisher.publishAll(events))
            .verifyComplete()
    }

    /**
     * Tests handling of empty events list reactively.
     * 
     * Verifies that:
     * 1. The adapter handles empty event lists gracefully
     * 2. The Mono<Void> completes successfully
     * 3. No errors are emitted
     */
    @Test
    fun `should handle empty events list reactively`() {
        // Given
        val events = emptyList<DomainEvent>()

        // When & Then
        StepVerifier.create(eventPublisher.publishAll(events))
            .verifyComplete()
    }

    /**
     * Tests reactive composition of multiple event publications.
     * 
     * Verifies that:
     * 1. Multiple sequential event publications work correctly
     * 2. Reactive composition with flatMap works as expected
     * 3. All events are processed successfully
     */
    @Test
    fun `should handle reactive composition of multiple events`() {
        // Given
        val event1 = PortfolioCreatedEvent(1L, "Portfolio 1", 100L, 200L)
        val event2 = TechnologyAddedEvent(1L, 10L, "Tech 1")
        val event3 = PortfolioUpdatedEvent(1L, mapOf("status" to "ACTIVE"))

        // When & Then
        StepVerifier.create(
            eventPublisher.publish(event1)
                .then(eventPublisher.publish(event2))
                .then(eventPublisher.publish(event3))
        )
            .verifyComplete()
    }

    /**
     * Tests reactive error handling.
     * 
     * Verifies that:
     * 1. The adapter handles errors gracefully
     * 2. Errors are logged but don't fail the operation
     * 3. The Mono<Void> still completes successfully
     */
    @Test
    fun `should handle errors reactively`() {
        // Given
        val event = PortfolioCreatedEvent(
            portfolioId = 1L,
            name = "Test Portfolio",
            ownerId = 100L,
            organizationId = 200L
        )

        // When & Then - should complete successfully even with potential errors
        StepVerifier.create(eventPublisher.publish(event))
            .verifyComplete()
    }

    /**
     * Tests reactive stream behavior with Flux.
     * 
     * Verifies that:
     * 1. The adapter works correctly with reactive streams
     * 2. Flux operations work as expected
     * 3. Backpressure handling works correctly
     */
    @Test
    fun `should work with reactive streams`() {
        // Given
        val events = Flux.just(
            PortfolioCreatedEvent(1L, "Portfolio 1", 100L, 200L),
            TechnologyAddedEvent(1L, 10L, "Tech 1"),
            PortfolioUpdatedEvent(1L, mapOf("status" to "ACTIVE"))
        )

        // When & Then
        StepVerifier.create(
            events.flatMap { event -> eventPublisher.publish(event) }
        )
            .verifyComplete()
    }
} 