package com.company.techportfolio.portfolio.adapter.inbound.web

import com.company.techportfolio.shared.domain.event.DomainEvent
import com.company.techportfolio.shared.domain.port.EventPublisher
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToServerSentEvents

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
     * Streams all domain events as Server-Sent Events.
     * 
     * Provides a continuous stream of domain events as they occur in the system.
     * Clients can subscribe to this endpoint to receive real-time event notifications.
     * 
     * @return Flux<ServerSentEvent<DomainEvent>> containing the event stream
     */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    fun streamEvents(): Flux<org.springframework.web.reactive.function.server.ServerSentEvent<DomainEvent>> {
        logger.info("Client connected to event stream")
        
        return Flux.interval(Duration.ofSeconds(1))
            .flatMap { _ ->
                // In a real implementation, this would be connected to an event bus
                // For now, we'll simulate events
                simulateEvent()
            }
            .map { event ->
                org.springframework.web.reactive.function.server.ServerSentEvent.builder<DomainEvent>()
                    .id(event.eventId)
                    .event(event.eventType)
                    .data(event)
                    .comment("Event streamed at ${LocalDateTime.now()}")
                    .build()
            }
            .onErrorResume { error ->
                logger.error("Error in event stream: ${error.message}", error)
                Flux.empty()
            }
    }

    /**
     * Streams events of a specific type as Server-Sent Events.
     * 
     * Filters the event stream to only include events of the specified type.
     * Useful for clients that only need to monitor specific event types.
     * 
     * @param eventType The type of events to stream
     * @return Flux<ServerSentEvent<DomainEvent>> containing the filtered event stream
     */
    @GetMapping("/stream/{eventType}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    fun streamEventsByType(
        @PathVariable eventType: String
    ): Flux<org.springframework.web.reactive.function.server.ServerSentEvent<DomainEvent>> {
        logger.info("Client connected to event stream for type: $eventType")
        
        return streamEvents()
            .filter { sseEvent ->
                sseEvent.data()?.eventType == eventType
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
     * Simulates a domain event for demonstration purposes.
     * 
     * In a real implementation, this would be replaced with actual event
     * generation from the domain services.
     * 
     * @return Mono<DomainEvent> simulated event
     */
    private fun simulateEvent(): Mono<DomainEvent> {
        return Mono.fromCallable {
            val eventTypes = listOf(
                "PortfolioCreatedEvent",
                "PortfolioUpdatedEvent", 
                "TechnologyAddedEvent",
                "TechnologyRemovedEvent"
            )
            val randomType = eventTypes.random()
            
            when (randomType) {
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

    /**
     * Creates a test event for demonstration purposes.
     * 
     * @return DomainEvent test event
     */
    private fun createTestEvent(): DomainEvent {
        return com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent(
            portfolioId = 999L,
            name = "Test Portfolio from Controller",
            ownerId = 100L,
            organizationId = 50L
        )
    }
} 