package com.company.techportfolio.gateway.adapter.out.authorization

import com.company.techportfolio.gateway.domain.port.AuthorizationPort
import com.company.techportfolio.gateway.domain.port.AuthorizationResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject

@Component
class AuthorizationServiceAdapter(
    private val restTemplate: RestTemplate
) : AuthorizationPort {

    @Value("\${services.authorization.url:http://localhost:8081}")
    private lateinit var authorizationServiceUrl: String

    override fun authorizeUser(username: String, resource: String, action: String): AuthorizationResult {
        return try {
            val url = "$authorizationServiceUrl/api/authorization/check"
            val request = mapOf(
                "username" to username,
                "resource" to resource,
                "action" to action
            )
            
            val response = restTemplate.postForObject<AuthorizationResult>(url, request)
            response ?: AuthorizationResult.unauthorized(username, resource, action, "No response from authorization service")
        } catch (e: Exception) {
            AuthorizationResult.unauthorized(username, resource, action, "Authorization service error: ${e.message}")
        }
    }

    override fun getUserPermissions(username: String): List<String> {
        return try {
            val url = "$authorizationServiceUrl/api/authorization/permissions?username=$username"
            val permissions = restTemplate.getForObject<List<String>>(url)
            permissions ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun hasRole(username: String, role: String): Boolean {
        return try {
            val url = "$authorizationServiceUrl/api/authorization/has-role?username=$username&role=$role"
            val hasRole = restTemplate.getForObject<Boolean>(url)
            hasRole ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun hasAnyRole(username: String, roles: List<String>): Boolean {
        return try {
            val url = "$authorizationServiceUrl/api/authorization/has-any-role"
            val request = mapOf(
                "username" to username,
                "roles" to roles
            )
            
            val hasAnyRole = restTemplate.postForObject<Boolean>(url, request)
            hasAnyRole ?: false
        } catch (e: Exception) {
            false
        }
    }
} 