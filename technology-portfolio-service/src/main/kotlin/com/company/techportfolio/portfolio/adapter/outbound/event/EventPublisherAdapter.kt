package com.company.techportfolio.portfolio.adapter.out.event

import com.company.techportfolio.shared.domain.event.DomainEvent
import com.company.techportfolio.shared.domain.port.EventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Reactive Event Publisher Adapter
 *
 * This adapter implements the EventPublisher interface using reactive programming patterns.
 * It provides non-blocking event publishing capabilities with proper error handling
 * and reactive composition.
 *
 * ## Features:
 * - Reactive event publishing with Mono<Void> return types
 * - Non-blocking event processing
 * - Reactive error handling with onErrorResume
 * - Support for event batching and streaming
 * - Reactive logging integration
 *
 * ## Event Types Supported:
 * - PortfolioCreatedEvent
 * - PortfolioUpdatedEvent
 * - TechnologyAddedEvent
 * - TechnologyRemovedEvent
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Component
class EventPublisherAdapter : EventPublisher {

    private val logger = LoggerFactory.getLogger(EventPublisherAdapter::class.java)

    /**
     * Publishes a single domain event reactively.
     *
     * @param event The domain event to publish
     * @return Mono<Void> indicating completion of the publish operation
     */
    override fun publish(event: DomainEvent): Mono<Void> {
        return Mono.fromCallable {
            logger.info("Publishing domain event: ${event.javaClass.simpleName} - ${event.eventId}")
            event
        }
            .flatMap { domainEvent ->
                when (domainEvent) {
                    is com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent -> {
                        logger.info("Portfolio created: ${domainEvent.portfolioId} - ${domainEvent.name}")
                        Mono.empty<Void>()
                    }

                    is com.company.techportfolio.shared.domain.event.PortfolioUpdatedEvent -> {
                        logger.info("Portfolio updated: ${domainEvent.portfolioId} - changes: ${domainEvent.changes}")
                        Mono.empty<Void>()
                    }

                    is com.company.techportfolio.shared.domain.event.TechnologyAddedEvent -> {
                        logger.info("Technology added: ${domainEvent.technologyId} - ${domainEvent.technologyName} to portfolio: ${domainEvent.portfolioId}")
                        Mono.empty<Void>()
                    }

                    is com.company.techportfolio.shared.domain.event.TechnologyRemovedEvent -> {
                        logger.info("Technology removed: ${domainEvent.technologyId} - ${domainEvent.technologyName} from portfolio: ${domainEvent.portfolioId}")
                        Mono.empty<Void>()
                    }

                    else -> {
                        logger.info("Unknown event type: ${domainEvent.javaClass.simpleName}")
                        Mono.empty<Void>()
                    }
                }
            }
            .onErrorResume { error ->
                logger.error("Failed to publish event ${event.eventType}: ${error.message}", error)
                Mono.empty<Void>()
            }
            .then()
    }

    /**
     * Publishes multiple domain events reactively.
     *
     * @param events The list of domain events to publish
     * @return Mono<Void> indicating completion of all publish operations
     */
    override fun publishAll(events: List<DomainEvent>): Mono<Void> {
        return if (events.isEmpty()) {
            logger.info("No events to publish")
            Mono.empty<Void>()
        } else {
            logger.info("Publishing ${events.size} domain events")
            Flux.fromIterable(events)
                .flatMap { event -> publish(event) }
                .then()
                .onErrorResume { error ->
                    logger.error("Failed to publish events batch: ${error.message}", error)
                    Mono.empty<Void>()
                }
        }
    }
} 