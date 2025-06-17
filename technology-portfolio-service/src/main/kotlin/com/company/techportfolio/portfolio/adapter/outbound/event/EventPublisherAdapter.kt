package com.company.techportfolio.portfolio.adapter.out.event

import com.company.techportfolio.shared.domain.port.EventPublisher
import com.company.techportfolio.shared.domain.event.DomainEvent
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Component
class EventPublisherAdapter : EventPublisher {
    
    private val logger = LoggerFactory.getLogger(EventPublisherAdapter::class.java)

    override fun publish(event: DomainEvent) {
        logger.info("Publishing domain event: ${event.javaClass.simpleName} - ${event.eventId}")
        
        // TODO: Implement actual event publishing logic
        // This could be:
        // 1. Publishing to a message broker (Kafka, RabbitMQ, etc.)
        // 2. Publishing to an event store
        // 3. Publishing to other microservices via HTTP
        // 4. Publishing to a notification service
        
        // For now, just log the event
        when (event) {
            is com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent -> {
                logger.info("Portfolio created: ${event.portfolioId} - ${event.name}")
            }
            is com.company.techportfolio.shared.domain.event.PortfolioUpdatedEvent -> {
                logger.info("Portfolio updated: ${event.portfolioId} - changes: ${event.changes}")
            }
            is com.company.techportfolio.shared.domain.event.TechnologyAddedEvent -> {
                logger.info("Technology added: ${event.technologyId} - ${event.technologyName} to portfolio: ${event.portfolioId}")
            }
            is com.company.techportfolio.shared.domain.event.TechnologyRemovedEvent -> {
                logger.info("Technology removed: ${event.technologyId} - ${event.technologyName} from portfolio: ${event.portfolioId}")
            }
            else -> {
                logger.info("Unknown event type: ${event.javaClass.simpleName}")
            }
        }
    }

    override fun publishAll(events: List<DomainEvent>) {
        logger.info("Publishing ${events.size} domain events")
        events.forEach { publish(it) }
    }
} 