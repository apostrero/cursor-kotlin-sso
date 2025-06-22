package com.company.techportfolio.authorization.domain.model

/**
 * Domain model representing an authorization request.
 *
 * This data class encapsulates all the information needed to make an authorization
 * decision, including the user requesting access, the resource being accessed,
 * the action being performed, and any additional context information.
 *
 * Authorization requests are typically created by the API Gateway or other services
 * when they need to verify if a user has permission to perform a specific action
 * on a particular resource within the technology portfolio system.
 *
 * Example usage:
 * ```kotlin
 * val request = AuthorizationRequest(
 *     username = "john.doe",
 *     resource = "portfolio:123",
 *     action = "READ",
 *     context = mapOf("department" to "Engineering")
 * )
 * ```
 *
 * @property username The unique identifier of the user requesting authorization
 * @property resource The resource being accessed (e.g., "portfolio:123", "technology:456")
 * @property action The action being performed (e.g., "READ", "WRITE", "DELETE")
 * @property context Additional context information that may influence authorization decisions
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class AuthorizationRequest(
    /** The unique identifier of the user requesting authorization */
    val username: String,
    /** The resource being accessed (e.g., "portfolio:123", "technology:456") */
    val resource: String,
    /** The action being performed (e.g., "READ", "WRITE", "DELETE") */
    val action: String,
    /** Additional context information that may influence authorization decisions */
    val context: Map<String, Any> = emptyMap()
) 