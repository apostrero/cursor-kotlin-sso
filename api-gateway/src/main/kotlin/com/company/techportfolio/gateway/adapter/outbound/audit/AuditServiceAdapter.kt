package com.company.techportfolio.gateway.adapter.out.audit

import com.company.techportfolio.gateway.domain.port.AuditPort
import com.company.techportfolio.gateway.domain.port.AuthenticationEvent
import com.company.techportfolio.gateway.domain.port.AuthorizationEvent
import com.company.techportfolio.gateway.domain.port.TokenEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@Component
class AuditServiceAdapter(
    private val restTemplate: RestTemplate
) : AuditPort {

    @Value("\${services.audit.url:http://localhost:8084}")
    private lateinit var auditServiceUrl: String

    override fun logAuthenticationEvent(event: AuthenticationEvent) {
        try {
            val url = "$auditServiceUrl/api/audit/authentication"
            restTemplate.postForObject<Unit>(url, event)
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            println("Failed to log authentication event: ${e.message}")
        }
    }

    override fun logAuthorizationEvent(event: AuthorizationEvent) {
        try {
            val url = "$auditServiceUrl/api/audit/authorization"
            restTemplate.postForObject<Unit>(url, event)
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            println("Failed to log authorization event: ${e.message}")
        }
    }

    override fun logTokenEvent(event: TokenEvent) {
        try {
            val url = "$auditServiceUrl/api/audit/token"
            restTemplate.postForObject<Unit>(url, event)
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            println("Failed to log token event: ${e.message}")
        }
    }
} 