package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.model.PortfolioStatus
import com.company.techportfolio.shared.model.PortfolioType
import com.company.techportfolio.shared.model.TechnologyPortfolio

/**
 * Repository interface for technology portfolio persistence operations.
 *
 * This interface defines the contract for data access operations related to
 * technology portfolios within the portfolio service. It follows the hexagonal
 * architecture pattern by defining domain requirements without depending on
 * specific implementations.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface TechnologyPortfolioRepository {
    fun findById(id: Long): TechnologyPortfolio?
    fun findByName(name: String): TechnologyPortfolio?
    fun findByOwnerId(ownerId: Long): List<TechnologyPortfolio>
    fun findByOrganizationId(organizationId: Long): List<TechnologyPortfolio>
    fun save(portfolio: TechnologyPortfolio): TechnologyPortfolio
    fun update(portfolio: TechnologyPortfolio): TechnologyPortfolio
    fun delete(id: Long): Boolean
    fun findAll(): List<TechnologyPortfolio>
    fun findByType(type: PortfolioType): List<TechnologyPortfolio>
    fun findByStatus(status: PortfolioStatus): List<TechnologyPortfolio>
} 