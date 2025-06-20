package com.company.techportfolio.gateway.adapter.out.event

import com.company.techportfolio.shared.domain.event.*
import com.company.techportfolio.shared.domain.port.EventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

/**
 * REST client adapter for domain event publishing across microservices.
 * 
 * This adapter implements the EventPublisher interface as an outbound adapter
 * in the hexagonal architecture, providing event-driven communication with
 * external microservices via HTTP REST API calls.
 * 
 * Key features:
 * - Domain event publishing to appropriate microservices
 * - Event routing based on event type
 * - Bulk event publishing capabilities
 * - Error handling with graceful degradation
 * - Configurable service endpoints
 * 
 * Event routing:
 * - Authentication/Authorization events → Audit service
 * - User management events → User management service
 * - Default events → Audit service
 * 
 * Configuration properties:
 * - services.audit.url: Base URL of the audit service
 * - services.user-management.url: Base URL of the user management service
 * 
 * @property restTemplate HTTP client for making REST API calls
 * @property auditServiceUrl Base URL of the audit service
 * @property userManagementServiceUrl Base URL of the user management service
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
/**
 * REST client adapter for domain event publishing across microservices.
 * 
 * This adapter implements the EventPublisher interface as an outbound adapter
 * in the hexagonal architecture, providing event-driven communication with
 * external microservices via HTTP REST API calls.
 * 
 * Key features:
 * - Domain event publishing to appropriate microservices
 * - Event routing based on event type
 * - Bulk event publishing capabilities
 * - Error handling with graceful degradation
 * - Configurable service endpoints
 * 
 * Event routing:
 * - Authentication/Authorization events → Audit service
 * - User management events → User management service
 * - Default events → Audit service
 * 
 * Configuration properties:
 * - services.audit.url: Base URL of the audit service
 * - services.user-management.url: Base URL of the user management service
 * 
 * @property restTemplate HTTP client for making REST API calls
 * @property auditServiceUrl Base URL of the audit service
 * @property userManagementServiceUrl Base URL of the user management service
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Component
class EventPublisherAdapter(
    private val restTemplate: RestTemplate
) : EventPublisher {

    @Value("\${services.audit.url:http://localhost:8084}")
    private lateinit var auditServiceUrl: String

    @Value("\${services.user-management.url:http://localhost:8083}")
    private lateinit var userManagementServiceUrl: String

    /**
     * Publishes a domain event to the appropriate microservice based on event type.
     * 
     * Routes events to different services based on their type:
     * - Authentication/Authorization events go to the audit service
     * - User management events go to the user management service
     * - Other events default to the audit service
     * 
     * Handles service communication errors gracefully without failing the main operation.
     * 
     * @param event Domain event to publish
     */
    /**
     * Publishes a domain event to the appropriate microservice based on event type.
     * 
     * Routes events to different services based on their type:
     * - Authentication/Authorization events go to the audit service
     * - User management events go to the user management service
     * - Other events default to the audit service
     * 
     * Handles service communication errors gracefully without failing the main operation.
     * 
     * @param event Domain event to publish
     */
    override fun publish(event: com.company.techportfolio.shared.domain.event.DomainEvent) {
        try {
            when (event) {
                is UserAuthenticatedEvent -> publishToAuditService(event)
                is UserAuthenticationFailedEvent -> publishToAuditService(event)
                is UserLoggedOutEvent -> publishToAuditService(event)
                is AuthorizationGrantedEvent -> publishToAuditService(event)
                is AuthorizationDeniedEvent -> publishToAuditService(event)
                is UserCreatedEvent -> publishToUserManagementService(event)
                is UserUpdatedEvent -> publishToUserManagementService(event)
                is UserDeactivatedEvent -> publishToUserManagementService(event)
                else -> publishToAuditService(event) // Default to audit service
            }
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            println("Failed to publish event ${event.eventType}: ${e.message}")
        }
    }

    /**
     * Publishes multiple domain events in sequence.
     * 
     * Iterates through the provided list of events and publishes each one
     * using the single event publishing logic. Each event is routed to
     * the appropriate service based on its type.
     * 
     * @param events List of domain events to publish
     */
    override fun publishAll(events: List<com.company.techportfolio.shared.domain.event.DomainEvent>) {
        events.forEach { publish(it) }
    }

    /**
     * Publishes a domain event to the audit service via REST API.
     * 
     * Makes an HTTP POST request to the audit service's event endpoint
     * to record the event for security monitoring and compliance.
     * 
     * @param event Domain event to publish to the audit service
     */
    private fun publishToAuditService(event: com.company.techportfolio.shared.domain.event.DomainEvent) {
        val url = "$auditServiceUrl/api/audit/events"
        restTemplate.postForObject<Unit>(url, event)
    }

    /**
     * Publishes a domain event to the user management service via REST API.
     * 
     * Makes an HTTP POST request to the user management service's event endpoint
     * to notify the service of user-related changes and updates.
     * 
     * @param event Domain event to publish to the user management service
     */
    private fun publishToUserManagementService(event: com.company.techportfolio.shared.domain.event.DomainEvent) {
        val url = "$userManagementServiceUrl/api/users/events"
        restTemplate.postForObject<Unit>(url, event)
    }
} 