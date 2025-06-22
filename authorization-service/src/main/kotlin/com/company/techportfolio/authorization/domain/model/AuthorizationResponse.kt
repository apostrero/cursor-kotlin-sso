package com.company.techportfolio.authorization.domain.model

/**
 * Domain model representing the result of an authorization request.
 *
 * This data class contains the outcome of an authorization decision, including
 * whether access is granted, user information, applicable permissions and roles,
 * and any error messages if authorization fails.
 *
 * The response provides comprehensive information that can be used by calling
 * services to make informed decisions about resource access and to provide
 * meaningful feedback to users about their authorization status.
 *
 * Example usage:
 * ```kotlin
 * val response = AuthorizationResponse.authorized(
 *     username = "john.doe",
 *     resource = "portfolio:123",
 *     action = "READ",
 *     permissions = listOf("READ_PORTFOLIO", "VIEW_ANALYTICS"),
 *     roles = listOf("PORTFOLIO_MANAGER")
 * )
 * ```
 *
 * @property isAuthorized Whether the authorization request was granted
 * @property username The username that was evaluated for authorization
 * @property resource The resource that was being accessed (optional)
 * @property action The action that was being performed (optional)
 * @property permissions List of specific permissions the user has
 * @property roles List of roles assigned to the user
 * @property organizationId The organization context for the authorization
 * @property errorMessage Error message if authorization failed
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class AuthorizationResponse(
    /** Whether the authorization request was granted */
    val isAuthorized: Boolean,
    /** The username that was evaluated for authorization */
    val username: String,
    /** The resource that was being accessed (optional) */
    val resource: String? = null,
    /** The action that was being performed (optional) */
    val action: String? = null,
    /** List of specific permissions the user has */
    val permissions: List<String> = emptyList(),
    /** List of roles assigned to the user */
    val roles: List<String> = emptyList(),
    /** The organization context for the authorization */
    val organizationId: Long? = null,
    /** Error message if authorization failed */
    val errorMessage: String? = null
) {
    companion object {
        /**
         * Creates an authorized response with user permissions and roles.
         *
         * This factory method creates a successful authorization response
         * containing the user's permissions, roles, and other relevant
         * authorization context information.
         *
         * @param username The username that was authorized
         * @param resource The resource that was accessed (optional)
         * @param action The action that was performed (optional)
         * @param permissions List of specific permissions granted
         * @param roles List of roles assigned to the user
         * @param organizationId The organization context (optional)
         * @return AuthorizationResponse indicating successful authorization
         */
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

        /**
         * Creates an unauthorized response with error information.
         *
         * This factory method creates a failed authorization response
         * with optional error message explaining why authorization
         * was denied.
         *
         * @param username The username that was denied authorization
         * @param resource The resource that was being accessed (optional)
         * @param action The action that was being performed (optional)
         * @param errorMessage Descriptive error message (optional)
         * @return AuthorizationResponse indicating failed authorization
         */
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