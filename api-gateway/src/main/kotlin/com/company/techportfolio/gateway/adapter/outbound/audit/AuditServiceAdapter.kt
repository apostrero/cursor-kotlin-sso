package com.company.techportfolio.gateway.adapter.out.audit

import com.company.techportfolio.gateway.domain.port.AuditPort
import com.company.techportfolio.gateway.domain.port.AuthenticationEvent
import com.company.techportfolio.gateway.domain.port.AuthorizationEvent
import com.company.techportfolio.gateway.domain.port.TokenEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

/**
 * REST client adapter for audit service communication.
 *
 * This adapter implements the AuditPort interface as an outbound adapter
 * in the hexagonal architecture, providing communication with the external
 * audit microservice via HTTP REST API calls for security event logging.
 *
 * Key features:
 * - Authentication event logging via REST API
 * - Authorization event logging for compliance
 * - Token lifecycle event logging
 * - Error handling with graceful degradation
 * - Configurable audit service endpoint
 *
 * The adapter ensures that audit logging failures do not impact the main
 * business operations by catching and logging errors without rethrowing them.
 *
 * Configuration properties:
 * - services.audit.url: Base URL of the audit service
 *
 * @property restTemplate HTTP client for making REST API calls
 * @property auditServiceUrl Base URL of the audit service
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Component
class AuditServiceAdapter(
    private val restTemplate: RestTemplate
) : AuditPort {

    private val logger: Logger = LoggerFactory.getLogger(AuditServiceAdapter::class.java)

    @Value("\${services.audit.url:http://localhost:8084}")
    private lateinit var auditServiceUrl: String

    /**
     * Logs an authentication-related event to the audit service via REST API.
     *
     * Makes an HTTP POST request to the audit service to record authentication
     * events for security monitoring and compliance. Handles service communication
     * errors gracefully without failing the main operation.
     *
     * @param event AuthenticationEvent containing details of the authentication event
     */
    override fun logAuthenticationEvent(event: AuthenticationEvent) {
        try {
            val url = "$auditServiceUrl/api/audit/authentication"
            restTemplate.postForObject<Unit>(url, event)
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            logger.error("Failed to log authentication event: ${e.message}")
        }
    }

    /**
     * Logs an authorization-related event to the audit service via REST API.
     *
     * Makes an HTTP POST request to the audit service to record authorization
     * decisions for security auditing and compliance tracking. Handles service
     * communication errors gracefully without failing the main operation.
     *
     * @param event AuthorizationEvent containing details of the authorization decision
     */
    override fun logAuthorizationEvent(event: AuthorizationEvent) {
        try {
            val url = "$auditServiceUrl/api/audit/authorization"
            restTemplate.postForObject<Unit>(url, event)
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            logger.error("Failed to log authorization event: ${e.message}")
        }
    }

    /**
     * Logs a token-related event to the audit service via REST API.
     *
     * Makes an HTTP POST request to the audit service to record token lifecycle
     * events for security monitoring and troubleshooting. Handles service
     * communication errors gracefully without failing the main operation.
     *
     * @param event TokenEvent containing details of the token operation
     */
    override fun logTokenEvent(event: TokenEvent) {
        try {
            val url = "$auditServiceUrl/api/audit/token"
            restTemplate.postForObject<Unit>(url, event)
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            logger.error("Failed to log token event: ${e.message}")
        }
    }
} 