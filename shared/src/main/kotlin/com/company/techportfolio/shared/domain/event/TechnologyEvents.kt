package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a technology is added to a portfolio.
 */
data class TechnologyAddedEvent(
    val portfolioId: Long,
    val technologyId: Long,
    val technologyName: String
) : DomainEvent()

/**
 * Event fired when a technology is updated.
 */
data class TechnologyUpdatedEvent(
    val portfolioId: Long,
    val technologyId: Long,
    val technologyName: String,
    val changes: Map<String, String>
) : DomainEvent()

/**
 * Event fired when a technology is removed from a portfolio.
 */
data class TechnologyRemovedEvent(
    val portfolioId: Long,
    val technologyId: Long,
    val technologyName: String
) : DomainEvent() 