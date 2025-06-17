package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a user successfully authenticates.
 */
data class UserAuthenticatedEvent(
    val username: String,
    val sessionIndex: String?,
    val ipAddress: String?,
    val userAgent: String?
) : DomainEvent()

/**
 * Event fired when a user authentication fails.
 */
data class UserAuthenticationFailedEvent(
    val username: String?,
    val reason: String,
    val ipAddress: String?,
    val userAgent: String?
) : DomainEvent()

/**
 * Event fired when a user logs out.
 */
data class UserLoggedOutEvent(
    val username: String,
    val sessionIndex: String?
) : DomainEvent() 