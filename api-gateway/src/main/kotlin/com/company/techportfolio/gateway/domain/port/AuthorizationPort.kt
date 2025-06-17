package com.company.techportfolio.gateway.domain.port

interface AuthorizationPort {
    fun authorizeUser(username: String, resource: String, action: String): AuthorizationResult
    fun getUserPermissions(username: String): List<String>
    fun hasRole(username: String, role: String): Boolean
    fun hasAnyRole(username: String, roles: List<String>): Boolean
}

data class AuthorizationResult(
    val isAuthorized: Boolean,
    val username: String,
    val resource: String,
    val action: String,
    val permissions: List<String> = emptyList(),
    val errorMessage: String? = null
) {
    companion object {
        fun authorized(username: String, resource: String, action: String, permissions: List<String>): AuthorizationResult =
            AuthorizationResult(
                isAuthorized = true,
                username = username,
                resource = resource,
                action = action,
                permissions = permissions
            )

        fun unauthorized(username: String, resource: String, action: String, errorMessage: String? = null): AuthorizationResult =
            AuthorizationResult(
                isAuthorized = false,
                username = username,
                resource = resource,
                action = action,
                errorMessage = errorMessage
            )
    }
} 
 