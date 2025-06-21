package com.company.techportfolio.shared.domain.port

import com.company.techportfolio.shared.domain.event.DomainEvent
import reactor.core.publisher.Mono

/**
 * Event Publisher Port - Domain Interface (REACTIVE)
 *
 * This interface defines the contract for domain event publishing operations
 * within the hexagonal architecture using reactive programming patterns.
 *
 * ## Responsibilities:
 * - Publishing domain events asynchronously
 * - Ensuring event delivery and reliability
 * - Supporting reactive error handling
 * - Maintaining event ordering when required
 *
 * ## Reactive Design:
 * - All methods return Mono<Void> for non-blocking operations
 * - Supports reactive error handling with onErrorMap and onErrorResume
 * - Enables backpressure handling for high-volume event publishing
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see DomainEvent
 */
interface EventPublisher {
    /**
     * Publishes a single domain event asynchronously.
     *
     * @param event The domain event to publish
     * @return Mono<Void> indicating completion of the publish operation
     */
    fun publish(event: DomainEvent): Mono<Void>

    /**
     * Publishes multiple domain events asynchronously.
     *
     * @param events The list of domain events to publish
     * @return Mono<Void> indicating completion of all publish operations
     */
    fun publishAll(events: List<DomainEvent>): Mono<Void>
}

/**
 * Event Handler Port - Domain Interface (REACTIVE)
 *
 * This interface defines the contract for handling domain events
 * using reactive programming patterns.
 *
 * @param T The type of domain event this handler can process
 */
interface EventHandler<T : DomainEvent> {
    /**
     * Handles a domain event asynchronously.
     *
     * @param event The domain event to handle
     * @return Mono<Void> indicating completion of the handling operation
     */
    fun handle(event: T): Mono<Void>
}

/**
 * Event Store Port - Domain Interface (REACTIVE)
 *
 * This interface defines the contract for event storage operations
 * using reactive programming patterns.
 */
interface EventStore {
    /**
     * Saves a single domain event asynchronously.
     *
     * @param event The domain event to save
     * @return Mono<Void> indicating completion of the save operation
     */
    fun save(event: DomainEvent): Mono<Void>

    /**
     * Saves multiple domain events asynchronously.
     *
     * @param events The list of domain events to save
     * @return Mono<Void> indicating completion of all save operations
     */
    fun saveAll(events: List<DomainEvent>): Mono<Void>

    /**
     * Retrieves events for a specific aggregate asynchronously.
     *
     * @param aggregateId The ID of the aggregate
     * @return Mono<List<DomainEvent>> containing the events for the aggregate
     */
    fun getEvents(aggregateId: String): Mono<List<DomainEvent>>

    /**
     * Retrieves events by type asynchronously.
     *
     * @param eventType The type of events to retrieve
     * @return Mono<List<DomainEvent>> containing the events of the specified type
     */
    fun getEventsByType(eventType: String): Mono<List<DomainEvent>>
} 