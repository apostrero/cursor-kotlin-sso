package com.company.techportfolio.shared.domain.event

/**
 * Event fired when authorization is granted to a user.
 */
data class AuthorizationGrantedEvent(
    val username: String,
    val resource: String,
    val action: String,
    val permissions: List<String>,
    val roles: List<String>
) : DomainEvent()

/**
 * Event fired when authorization is denied to a user.
 */
data class AuthorizationDeniedEvent(
    val username: String,
    val resource: String,
    val action: String,
    val reason: String
) : DomainEvent() 