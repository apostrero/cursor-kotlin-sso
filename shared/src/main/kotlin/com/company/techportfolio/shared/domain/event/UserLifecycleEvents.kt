package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a new user is created.
 */
data class UserCreatedEvent(
    val userId: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String
) : DomainEvent()

/**
 * Event fired when a user's information is updated.
 */
data class UserUpdatedEvent(
    val userId: Long,
    val changes: Map<String, String>
) : DomainEvent()

/**
 * Event fired when a user account is deactivated.
 */
data class UserDeactivatedEvent(
    val userId: Long,
    val username: String,
    val reason: String?
) : DomainEvent()

/**
 * Event fired when a user account is activated.
 */
data class UserActivatedEvent(
    val userId: Long,
    val username: String
) : DomainEvent() 