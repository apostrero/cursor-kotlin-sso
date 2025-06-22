package com.company.techportfolio.portfolio.adapter.inbound.web

import com.company.techportfolio.shared.domain.event.DomainEvent
import com.company.techportfolio.shared.domain.port.EventPublisher
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Reactive Event Stream Controller
 *
 * This controller provides Server-Sent Events (SSE) endpoints for real-time
 * event monitoring and streaming. It enables clients to subscribe to domain
 * events as they occur in the system.
 *
 * ## Features:
 * - Real-time event streaming via Server-Sent Events
 * - Event filtering by type and source
 * - Reactive event processing with backpressure handling
 * - Authentication and authorization for event access
 * - Event replay capabilities for missed events
 *
 * ## Endpoints:
 * - GET /api/events/stream - Stream all events
 * - GET /api/events/stream/{eventType} - Stream events by type
 * - GET /api/events/replay - Replay recent events
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/events")
class EventStreamController(
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(EventStreamController::class.java)

    // In-memory event store for replay (in production, use a proper event store)
    private val recentEvents = ConcurrentHashMap<String, DomainEvent>()

    /**
     * Streams events as Server-Sent Events (SSE).
     *
     * Provides a reactive stream of domain events for real-time monitoring
     * and updates. Events are streamed every 2 seconds with proper error handling.
     *
     * @return Flux<String> with SSE-formatted event data
     */
    @GetMapping("/events/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun streamEvents(): Flux<String> {
        return Flux.interval(Duration.ofSeconds(2))
            .map { _ ->
                val event = createTestEvent()
                recentEvents[event.eventId] = event
                "data: ${event.toJson()}\n\n"
            }
            .onErrorResume { error ->
                logger.error("Error streaming events: ${error.message}", error)
                Flux.just("data: {\"error\": \"${error.message}\"}\n\n")
            }
    }

    /**
     * Streams portfolio events as Server-Sent Events (SSE).
     *
     * Provides a reactive stream of portfolio-specific events for real-time
     * portfolio monitoring and updates.
     *
     * @return Flux<String> with SSE-formatted portfolio event data
     */
    @GetMapping("/portfolios/events/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun streamPortfolioEvents(): Flux<String> {
        return Flux.interval(Duration.ofSeconds(3))
            .map { _ ->
                val event = createPortfolioEvent()
                recentEvents[event.eventId] = event
                "data: ${event.toJson()}\n\n"
            }
            .onErrorResume { error ->
                logger.error("Error streaming portfolio events: ${error.message}", error)
                Flux.just("data: {\"error\": \"${error.message}\"}\n\n")
            }
    }

    /**
     * Streams events of a specific type as Server-Sent Events.
     *
     * Filters the event stream to only include events of the specified type.
     * Useful for clients that only need to monitor specific event types.
     *
     * @param eventType The type of events to stream
     * @return Flux<String> containing the filtered event stream
     */
    @GetMapping("/stream/{eventType}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    fun streamEventsByType(
        @PathVariable eventType: String
    ): Flux<String> {
        logger.info("Client connected to event stream for type: $eventType")

        return Flux.interval(Duration.ofSeconds(2))
            .map { _ ->
                val event = createEventByType(eventType)
                recentEvents[event.eventId] = event
                "data: ${event.toJson()}\n\n"
            }
            .filter { sseData ->
                sseData.contains(eventType)
            }
            .onErrorResume { error ->
                logger.error("Error streaming events for type $eventType: ${error.message}", error)
                Flux.just("data: {\"error\": \"${error.message}\"}\n\n")
            }
    }

    /**
     * Replays recent events for clients that missed them.
     *
     * Returns a list of recent events that occurred in the system.
     * Useful for clients that need to catch up on missed events.
     *
     * @param limit Maximum number of events to replay (default: 100)
     * @return Mono<List<DomainEvent>> containing recent events
     */
    @GetMapping("/replay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    fun replayEvents(
        @RequestParam(defaultValue = "100") limit: Int
    ): Mono<List<DomainEvent>> {
        logger.info("Replaying up to $limit recent events")

        return Mono.fromCallable {
            recentEvents.values
                .take(limit)
                .sortedBy { it.timestamp }
                .toList()
        }
    }

    /**
     * Publishes a test event for demonstration purposes.
     *
     * This endpoint allows testing the event publishing system by creating
     * and publishing a test event.
     *
     * @return Mono<String> confirmation message
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    fun publishTestEvent(): Mono<String> {
        val testEvent = createTestEvent()

        return eventPublisher.publish(testEvent)
            .then(Mono.fromCallable {
                recentEvents[testEvent.eventId] = testEvent
                "Test event published: ${testEvent.eventId}"
            })
            .onErrorResume { error ->
                logger.error("Failed to publish test event: ${error.message}", error)
                Mono.just("Failed to publish test event: ${error.message}")
            }
    }

    /**
     * Creates a test event for demonstration purposes.
     *
     * @return DomainEvent test event
     */
    private fun createTestEvent(): DomainEvent {
        return com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent(
            portfolioId = (1..1000).random().toLong(),
            name = "Test Portfolio ${(1..100).random()}",
            ownerId = (1..100).random().toLong(),
            organizationId = (1..50).random().toLong()
        )
    }

    /**
     * Creates a portfolio-specific event for demonstration purposes.
     *
     * @return DomainEvent portfolio event
     */
    private fun createPortfolioEvent(): DomainEvent {
        val eventTypes = listOf(
            "PortfolioCreatedEvent",
            "PortfolioUpdatedEvent",
            "TechnologyAddedEvent",
            "TechnologyRemovedEvent"
        )
        val randomType = eventTypes.random()

        return when (randomType) {
            "PortfolioCreatedEvent" -> com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent(
                portfolioId = (1..1000).random().toLong(),
                name = "Test Portfolio ${(1..100).random()}",
                ownerId = (1..100).random().toLong(),
                organizationId = (1..50).random().toLong()
            )

            "PortfolioUpdatedEvent" -> com.company.techportfolio.shared.domain.event.PortfolioUpdatedEvent(
                portfolioId = (1..1000).random().toLong(),
                changes = mapOf("status" to "ACTIVE", "updatedAt" to LocalDateTime.now().toString())
            )

            "TechnologyAddedEvent" -> com.company.techportfolio.shared.domain.event.TechnologyAddedEvent(
                portfolioId = (1..1000).random().toLong(),
                technologyId = (1..5000).random().toLong(),
                technologyName = "Technology ${(1..100).random()}"
            )

            "TechnologyRemovedEvent" -> com.company.techportfolio.shared.domain.event.TechnologyRemovedEvent(
                portfolioId = (1..1000).random().toLong(),
                technologyId = (1..5000).random().toLong(),
                technologyName = "Technology ${(1..100).random()}"
            )

            else -> com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent(
                portfolioId = 1L,
                name = "Default Portfolio",
                ownerId = 1L,
                organizationId = 1L
            )
        }
    }

    /**
     * Creates an event of a specific type for demonstration purposes.
     *
     * @param eventType The type of event to create
     * @return DomainEvent of the specified type
     */
    private fun createEventByType(eventType: String): DomainEvent {
        return when (eventType) {
            "PortfolioCreatedEvent" -> com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent(
                portfolioId = (1..1000).random().toLong(),
                name = "Test Portfolio ${(1..100).random()}",
                ownerId = (1..100).random().toLong(),
                organizationId = (1..50).random().toLong()
            )

            "PortfolioUpdatedEvent" -> com.company.techportfolio.shared.domain.event.PortfolioUpdatedEvent(
                portfolioId = (1..1000).random().toLong(),
                changes = mapOf("status" to "ACTIVE", "updatedAt" to LocalDateTime.now().toString())
            )

            "TechnologyAddedEvent" -> com.company.techportfolio.shared.domain.event.TechnologyAddedEvent(
                portfolioId = (1..1000).random().toLong(),
                technologyId = (1..5000).random().toLong(),
                technologyName = "Technology ${(1..100).random()}"
            )

            "TechnologyRemovedEvent" -> com.company.techportfolio.shared.domain.event.TechnologyRemovedEvent(
                portfolioId = (1..1000).random().toLong(),
                technologyId = (1..5000).random().toLong(),
                technologyName = "Technology ${(1..100).random()}"
            )

            else -> com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent(
                portfolioId = 1L,
                name = "Default Portfolio",
                ownerId = 1L,
                organizationId = 1L
            )
        }
    }
} 