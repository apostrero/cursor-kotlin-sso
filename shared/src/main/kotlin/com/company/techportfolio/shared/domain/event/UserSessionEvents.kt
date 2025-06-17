package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a user logs in.
 */
data class UserLoginEvent(
    val userId: Long,
    val username: String,
    val loginSource: String,
    val ipAddress: String?
) : DomainEvent()

/**
 * Event fired when a user logs out.
 */
data class UserLogoutEvent(
    val userId: Long,
    val username: String,
    val sessionDuration: Long? // in seconds
) : DomainEvent() 