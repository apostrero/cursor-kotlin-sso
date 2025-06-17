package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a role is assigned to a user.
 */
data class UserRoleAssignedEvent(
    val userId: Long,
    val username: String,
    val role: String,
    val assignedBy: Long?
) : DomainEvent()

/**
 * Event fired when a role is removed from a user.
 */
data class UserRoleRemovedEvent(
    val userId: Long,
    val username: String,
    val role: String,
    val removedBy: Long?
) : DomainEvent() 