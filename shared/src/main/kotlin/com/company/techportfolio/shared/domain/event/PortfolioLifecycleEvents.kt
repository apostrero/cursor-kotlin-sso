package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a new portfolio is created.
 */
data class PortfolioCreatedEvent(
    val portfolioId: Long,
    val name: String,
    val ownerId: Long,
    val organizationId: Long?
) : DomainEvent()

/**
 * Event fired when a portfolio is updated.
 */
data class PortfolioUpdatedEvent(
    val portfolioId: Long,
    val changes: Map<String, String>
) : DomainEvent()

/**
 * Event fired when a portfolio is deleted.
 */
data class PortfolioDeletedEvent(
    val portfolioId: Long,
    val name: String,
    val ownerId: Long
) : DomainEvent()

/**
 * Event fired when a portfolio's status changes.
 */
data class PortfolioStatusChangedEvent(
    val portfolioId: Long,
    val oldStatus: String,
    val newStatus: String,
    val reason: String?
) : DomainEvent() 