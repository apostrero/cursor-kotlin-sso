package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.portfolio.domain.model.PortfolioSummary

interface PortfolioQueryRepository {
    fun findPortfolioSummary(id: Long): PortfolioSummary?
    fun findPortfolioSummariesByOwner(ownerId: Long): List<PortfolioSummary>
    fun findPortfolioSummariesByOrganization(organizationId: Long): List<PortfolioSummary>
    fun findPortfolioSummariesByType(type: PortfolioType): List<PortfolioSummary>
    fun findPortfolioSummariesByStatus(status: PortfolioStatus): List<PortfolioSummary>
    fun findAllPortfolioSummaries(): List<PortfolioSummary>
    fun searchPortfolios(name: String?, type: PortfolioType?, status: PortfolioStatus?, organizationId: Long?): List<PortfolioSummary>
} 