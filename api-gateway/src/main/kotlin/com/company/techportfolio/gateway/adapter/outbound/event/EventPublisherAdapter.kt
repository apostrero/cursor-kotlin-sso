package com.company.techportfolio.gateway.adapter.out.event

import com.company.techportfolio.shared.domain.event.*
import com.company.techportfolio.shared.domain.port.EventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@Component
class EventPublisherAdapter(
    private val restTemplate: RestTemplate
) : EventPublisher {

    @Value("\${services.audit.url:http://localhost:8084}")
    private lateinit var auditServiceUrl: String

    @Value("\${services.user-management.url:http://localhost:8083}")
    private lateinit var userManagementServiceUrl: String

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

    override fun publishAll(events: List<com.company.techportfolio.shared.domain.event.DomainEvent>) {
        events.forEach { publish(it) }
    }

    private fun publishToAuditService(event: com.company.techportfolio.shared.domain.event.DomainEvent) {
        val url = "$auditServiceUrl/api/audit/events"
        restTemplate.postForObject<Unit>(url, event)
    }

    private fun publishToUserManagementService(event: com.company.techportfolio.shared.domain.event.DomainEvent) {
        val url = "$userManagementServiceUrl/api/users/events"
        restTemplate.postForObject<Unit>(url, event)
    }
} 