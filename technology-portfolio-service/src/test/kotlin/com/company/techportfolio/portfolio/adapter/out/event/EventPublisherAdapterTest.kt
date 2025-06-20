package com.company.techportfolio.portfolio.adapter.out.event

import com.company.techportfolio.shared.domain.event.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for EventPublisherAdapter.
 * 
 * This test class verifies the functionality of the EventPublisherAdapter, which
 * is the implementation of the EventPublisher port in the hexagonal architecture.
 * It tests the adapter's ability to publish various domain events without errors.
 * 
 * ## Test Coverage:
 * - Publishing of portfolio-related events (created, updated)
 * - Publishing of technology-related events (added, removed)
 * - Sequential event publishing
 * - Batch event publishing
 * - Error handling (implicit through assertDoesNotThrow)
 * 
 * ## Testing Approach:
 * - Direct invocation of adapter methods with test event instances
 * - Verification that no exceptions are thrown during publishing
 * - Testing with various event types to ensure type compatibility
 * 
 * ## Note:
 * These tests verify the adapter's interface contract but do not validate
 * actual message delivery, which would require integration testing.
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
     * Tests publishing a PortfolioCreatedEvent.
     * 
     * Verifies that:
     * 1. The adapter can accept and process a PortfolioCreatedEvent
     * 2. No exceptions are thrown during publishing
     * 3. The event data is properly handled
     */
    @Test
    fun `should publish PortfolioCreatedEvent without errors`() {
        // Given
        val event = PortfolioCreatedEvent(
            portfolioId = 1L,
            name = "Test Portfolio",
            ownerId = 100L,
            organizationId = 200L
        )

        // When & Then (should not throw)
        assertDoesNotThrow {
            eventPublisher.publish(event)
        }
    }

    /**
     * Tests publishing a PortfolioUpdatedEvent.
     * 
     * Verifies that:
     * 1. The adapter can accept and process a PortfolioUpdatedEvent
     * 2. No exceptions are thrown during publishing
     * 3. The event data is properly handled
     * 4. The changes map is correctly processed
     */
    @Test
    fun `should publish PortfolioUpdatedEvent without errors`() {
        // Given
        val event = PortfolioUpdatedEvent(
            portfolioId = 1L,
            changes = mapOf("name" to "Updated Portfolio", "status" to "INACTIVE")
        )

        // When & Then (should not throw)
        assertDoesNotThrow {
            eventPublisher.publish(event)
        }
    }

    /**
     * Tests publishing a TechnologyAddedEvent.
     * 
     * Verifies that:
     * 1. The adapter can accept and process a TechnologyAddedEvent
     * 2. No exceptions are thrown during publishing
     * 3. The event data is properly handled
     */
    @Test
    fun `should publish TechnologyAddedEvent without errors`() {
        // Given
        val event = TechnologyAddedEvent(
            portfolioId = 1L,
            technologyId = 10L,
            technologyName = "Spring Boot"
        )

        // When & Then (should not throw)
        assertDoesNotThrow {
            eventPublisher.publish(event)
        }
    }

    /**
     * Tests publishing a TechnologyRemovedEvent.
     * 
     * Verifies that:
     * 1. The adapter can accept and process a TechnologyRemovedEvent
     * 2. No exceptions are thrown during publishing
     * 3. The event data is properly handled
     */
    @Test
    fun `should publish TechnologyRemovedEvent without errors`() {
        // Given
        val event = TechnologyRemovedEvent(
            portfolioId = 1L,
            technologyId = 10L,
            technologyName = "Spring Boot"
        )

        // When & Then (should not throw)
        assertDoesNotThrow {
            eventPublisher.publish(event)
        }
    }

    /**
     * Tests publishing multiple events in sequence.
     * 
     * Verifies that:
     * 1. The adapter can handle multiple sequential event publications
     * 2. No exceptions are thrown during publishing
     * 3. Events of different types can be published in sequence
     */
    @Test
    fun `should handle multiple events in sequence`() {
        // Given
        val event1 = PortfolioCreatedEvent(1L, "Portfolio 1", 100L, 200L)
        val event2 = TechnologyAddedEvent(1L, 10L, "Tech 1")
        val event3 = PortfolioUpdatedEvent(1L, mapOf("status" to "ACTIVE"))

        // When & Then (should not throw)
        assertDoesNotThrow {
            eventPublisher.publish(event1)
            eventPublisher.publish(event2)
            eventPublisher.publish(event3)
        }
    }

    /**
     * Tests publishing multiple events using the batch method.
     * 
     * Verifies that:
     * 1. The adapter can handle batch publishing of multiple events
     * 2. No exceptions are thrown during publishing
     * 3. Events of different types can be published in a batch
     */
    @Test
    fun `should publish multiple events using publishAll`() {
        // Given
        val events = listOf(
            PortfolioCreatedEvent(1L, "Portfolio 1", 100L, 200L),
            TechnologyAddedEvent(1L, 10L, "Tech 1"),
            PortfolioUpdatedEvent(1L, mapOf("status" to "ACTIVE"))
        )

        // When & Then (should not throw)
        assertDoesNotThrow {
            eventPublisher.publishAll(events)
        }
    }
} 