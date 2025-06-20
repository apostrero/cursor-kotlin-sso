package com.company.techportfolio.gateway.adapter.out.event

import com.company.techportfolio.shared.domain.event.*
import com.company.techportfolio.shared.domain.port.EventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Reactive REST client adapter for domain event publishing across microservices.
 * 
 * This adapter implements the EventPublisher interface as an outbound adapter
 * in the hexagonal architecture, providing reactive event-driven communication
 * with external microservices via HTTP REST API calls using WebClient.
 * 
 * ## Key Features:
 * - Reactive domain event publishing to appropriate microservices
 * - Event routing based on event type with reactive composition
 * - Bulk event publishing capabilities with backpressure handling
 * - Reactive error handling with graceful degradation
 * - Configurable service endpoints with timeout management
 * - Circuit breaker pattern integration
 * 
 * ## Event Routing:
 * - Authentication/Authorization events → Audit service
 * - User management events → User management service
 * - Default events → Audit service
 * 
 * ## Configuration Properties:
 * - services.audit.url: Base URL of the audit service
 * - services.user-management.url: Base URL of the user management service
 * - services.timeout: HTTP request timeout (default: 5s)
 * 
 * ## Reactive Design:
 * - Uses WebClient for non-blocking HTTP requests
 * - Returns Mono<Void> for reactive composition
 * - Implements reactive error handling with onErrorResume
 * - Supports backpressure handling for high-volume events
 * 
 * @property webClient Reactive HTTP client for making REST API calls
 * @property auditServiceUrl Base URL of the audit service
 * @property userManagementServiceUrl Base URL of the user management service
 * @property timeout HTTP request timeout duration
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Component
class EventPublisherAdapter(
    private val webClient: WebClient
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(EventPublisherAdapter::class.java)

    @Value("\${services.audit.url:http://localhost:8084}")
    private lateinit var auditServiceUrl: String

    @Value("\${services.user-management.url:http://localhost:8083}")
    private lateinit var userManagementServiceUrl: String

    @Value("\${services.timeout:5s}")
    private lateinit var timeout: String

    /**
     * Publishes a domain event to the appropriate microservice based on event type.
     * 
     * Routes events to different services based on their type using reactive patterns:
     * - Authentication/Authorization events go to the audit service
     * - User management events go to the user management service
     * - Other events default to the audit service
     * 
     * Handles service communication errors reactively without failing the main operation.
     * 
     * @param event Domain event to publish
     * @return Mono<Void> indicating completion of the publish operation
     */
    override fun publish(event: com.company.techportfolio.shared.domain.event.DomainEvent): Mono<Void> {
        return Mono.fromCallable {
            logger.info("Publishing domain event: ${event.eventType} - ${event.eventId}")
            event
        }
        .flatMap { domainEvent ->
            when (domainEvent) {
                is UserAuthenticatedEvent -> publishToAuditService(domainEvent)
                is UserAuthenticationFailedEvent -> publishToAuditService(domainEvent)
                is UserLoggedOutEvent -> publishToAuditService(domainEvent)
                is AuthorizationGrantedEvent -> publishToAuditService(domainEvent)
                is AuthorizationDeniedEvent -> publishToAuditService(domainEvent)
                is UserCreatedEvent -> publishToUserManagementService(domainEvent)
                is UserUpdatedEvent -> publishToUserManagementService(domainEvent)
                is UserDeactivatedEvent -> publishToUserManagementService(domainEvent)
                else -> publishToAuditService(domainEvent) // Default to audit service
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
     * Processes the provided list of events using reactive streams with backpressure
     * handling. Each event is routed to the appropriate service based on its type.
     * 
     * @param events List of domain events to publish
     * @return Mono<Void> indicating completion of all publish operations
     */
    override fun publishAll(events: List<com.company.techportfolio.shared.domain.event.DomainEvent>): Mono<Void> {
        return if (events.isEmpty()) {
            logger.info("No events to publish")
            Mono.empty<Void>()
        } else {
            logger.info("Publishing ${events.size} domain events reactively")
            Flux.fromIterable(events)
                .flatMap { event -> publish(event) }
                .then()
                .onErrorResume { error ->
                    logger.error("Failed to publish events batch: ${error.message}", error)
                    Mono.empty<Void>()
                }
        }
    }

    /**
     * Publishes a domain event to the audit service via reactive REST API.
     * 
     * Makes a non-blocking HTTP POST request to the audit service's event endpoint
     * to record the event for security monitoring and compliance.
     * 
     * @param event Domain event to publish to the audit service
     * @return Mono<Void> indicating completion of the publish operation
     */
    private fun publishToAuditService(event: com.company.techportfolio.shared.domain.event.DomainEvent): Mono<Void> {
        val url = "$auditServiceUrl/api/audit/events"
        return webClient.post()
            .uri(url)
            .bodyValue(event)
            .retrieve()
            .bodyToMono(Void::class.java)
            .timeout(Duration.parse("PT${timeout}"))
            .onErrorResume { error ->
                logger.error("Failed to publish to audit service: ${error.message}", error)
                Mono.empty<Void>()
            }
            .then()
    }

    /**
     * Publishes a domain event to the user management service via reactive REST API.
     * 
     * Makes a non-blocking HTTP POST request to the user management service's event endpoint
     * to notify the service of user-related changes and updates.
     * 
     * @param event Domain event to publish to the user management service
     * @return Mono<Void> indicating completion of the publish operation
     */
    private fun publishToUserManagementService(event: com.company.techportfolio.shared.domain.event.DomainEvent): Mono<Void> {
        val url = "$userManagementServiceUrl/api/users/events"
        return webClient.post()
            .uri(url)
            .bodyValue(event)
            .retrieve()
            .bodyToMono(Void::class.java)
            .timeout(Duration.parse("PT${timeout}"))
            .onErrorResume { error ->
                logger.error("Failed to publish to user management service: ${error.message}", error)
                Mono.empty<Void>()
            }
            .then()
    }
} 