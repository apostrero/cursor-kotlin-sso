package com.company.techportfolio.shared.domain.port

/**
 * Command to create a new user in the system.
 */
data class CreateUserCommand(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val organizationId: Long?,
    val roles: List<String> = emptyList()
) : Command()

/**
 * Command to update an existing user's information.
 */
data class UpdateUserCommand(
    val username: String,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val isActive: Boolean? = null,
    val organizationId: Long? = null
) : Command()

/**
 * Command to deactivate a user account.
 */
data class DeactivateUserCommand(
    val username: String,
    val reason: String
) : Command()

/**
 * Command to assign a role to a user.
 */
data class AssignRoleCommand(
    val username: String,
    val roleName: String
) : Command()

/**
 * Command to remove a role from a user.
 */
data class RemoveRoleCommand(
    val username: String,
    val roleName: String
) : Command() 