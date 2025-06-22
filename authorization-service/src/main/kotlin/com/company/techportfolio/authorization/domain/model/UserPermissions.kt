package com.company.techportfolio.authorization.domain.model

/**
 * Domain model representing a user's complete permission and role information.
 *
 * This data class aggregates all authorization-related information for a specific
 * user, including their assigned permissions, roles, organizational context, and
 * account status. It serves as a comprehensive view of a user's authorization
 * capabilities within the technology portfolio system.
 *
 * This model is typically used by the authorization service to cache user
 * permission information and make efficient authorization decisions without
 * repeatedly querying the database for the same user's permissions.
 *
 * Example usage:
 * ```kotlin
 * val userPermissions = UserPermissions(
 *     username = "john.doe",
 *     permissions = listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS"),
 *     roles = listOf("PORTFOLIO_MANAGER", "TEAM_LEAD"),
 *     organizationId = 123L,
 *     isActive = true
 * )
 * ```
 *
 * @property username The unique identifier of the user
 * @property permissions List of specific permissions granted to the user
 * @property roles List of roles assigned to the user
 * @property organizationId The organization the user belongs to (optional)
 * @property isActive Whether the user account is currently active
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class UserPermissions(
    /** The unique identifier of the user */
    val username: String,
    /** List of specific permissions granted to the user */
    val permissions: List<String> = emptyList(),
    /** List of roles assigned to the user */
    val roles: List<String> = emptyList(),
    /** The organization the user belongs to (optional) */
    val organizationId: Long? = null,
    /** Whether the user account is currently active */
    val isActive: Boolean = false
) 