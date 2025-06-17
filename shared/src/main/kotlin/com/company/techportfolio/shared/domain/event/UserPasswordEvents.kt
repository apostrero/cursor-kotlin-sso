package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a user's password is changed.
 */
data class UserPasswordChangedEvent(
    val userId: Long,
    val username: String,
    val changedBy: Long? // null if self-service
) : DomainEvent()

/**
 * Event fired when a password reset is requested for a user.
 */
data class UserPasswordResetRequestedEvent(
    val userId: Long,
    val username: String,
    val email: String,
    val resetToken: String
) : DomainEvent() 