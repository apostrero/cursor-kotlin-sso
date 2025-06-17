package com.company.techportfolio.authorization.domain.model

data class AuthorizationResponse(
    val isAuthorized: Boolean,
    val username: String,
    val resource: String? = null,
    val action: String? = null,
    val permissions: List<String> = emptyList(),
    val roles: List<String> = emptyList(),
    val organizationId: Long? = null,
    val errorMessage: String? = null
) {
    companion object {
        fun authorized(
            username: String,
            resource: String? = null,
            action: String? = null,
            permissions: List<String> = emptyList(),
            roles: List<String> = emptyList(),
            organizationId: Long? = null
        ): AuthorizationResponse {
            return AuthorizationResponse(
                isAuthorized = true,
                username = username,
                resource = resource,
                action = action,
                permissions = permissions,
                roles = roles,
                organizationId = organizationId
            )
        }

        fun unauthorized(
            username: String,
            resource: String? = null,
            action: String? = null,
            errorMessage: String? = null
        ): AuthorizationResponse {
            return AuthorizationResponse(
                isAuthorized = false,
                username = username,
                resource = resource,
                action = action,
                errorMessage = errorMessage
            )
        }
    }
} 