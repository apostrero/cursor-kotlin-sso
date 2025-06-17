package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a portfolio's cost information is updated.
 */
data class PortfolioCostUpdatedEvent(
    val portfolioId: Long,
    val oldTotalCost: String?,
    val newTotalCost: String?,
    val changeAmount: String?
) : DomainEvent() 