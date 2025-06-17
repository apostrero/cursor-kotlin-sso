package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.domain.model.TechnologyPortfolio
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.portfolio.domain.model.PortfolioSummary

interface PortfolioRepository {
    fun findById(id: Long): TechnologyPortfolio?
    fun findByName(name: String): TechnologyPortfolio?
    fun findByOwnerId(ownerId: Long): List<TechnologyPortfolio>
    fun findByOrganizationId(organizationId: Long): List<TechnologyPortfolio>
    fun findByType(type: PortfolioType): List<TechnologyPortfolio>
    fun findByStatus(status: PortfolioStatus): List<TechnologyPortfolio>
    fun findAll(): List<TechnologyPortfolio>
    fun save(portfolio: TechnologyPortfolio): TechnologyPortfolio
    fun update(portfolio: TechnologyPortfolio): TechnologyPortfolio
    fun delete(id: Long): Boolean
    fun existsById(id: Long): Boolean
    fun countByOwnerId(ownerId: Long): Long
    fun countByOrganizationId(organizationId: Long): Long
}