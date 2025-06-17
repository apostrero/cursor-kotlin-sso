package com.company.techportfolio.portfolio.adapter.out.persistence

import com.company.techportfolio.portfolio.adapter.out.persistence.repository.PortfolioJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.repository.TechnologyJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.entity.PortfolioEntity
import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.portfolio.domain.port.PortfolioRepository
import com.company.techportfolio.portfolio.domain.port.PortfolioQueryRepository
import com.company.techportfolio.shared.domain.model.TechnologyPortfolio
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.portfolio.domain.model.PortfolioSummary
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class PortfolioRepositoryAdapter(
    private val portfolioJpaRepository: PortfolioJpaRepository,
    private val technologyJpaRepository: TechnologyJpaRepository
) : PortfolioRepository, PortfolioQueryRepository {

    // PortfolioRepository implementation
    override fun findById(id: Long): TechnologyPortfolio? {
        return portfolioJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByName(name: String): TechnologyPortfolio? {
        return portfolioJpaRepository.findByName(name)?.toDomain()
    }

    override fun findByOwnerId(ownerId: Long): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByOwnerId(ownerId).map { it.toDomain() }
    }

    override fun findByOrganizationId(organizationId: Long): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByOrganizationId(organizationId).map { it.toDomain() }
    }

    override fun findByType(type: PortfolioType): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByType(type).map { it.toDomain() }
    }

    override fun findByStatus(status: PortfolioStatus): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByStatus(status).map { it.toDomain() }
    }

    override fun findAll(): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByIsActiveTrue().map { it.toDomain() }
    }

    override fun save(portfolio: TechnologyPortfolio): TechnologyPortfolio {
        val entity = portfolio.toEntity()
        val savedEntity = portfolioJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun update(portfolio: TechnologyPortfolio): TechnologyPortfolio {
        val entity = portfolio.toEntity()
        val savedEntity = portfolioJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun delete(id: Long): Boolean {
        return try {
            portfolioJpaRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun existsById(id: Long): Boolean {
        return portfolioJpaRepository.existsById(id)
    }

    override fun countByOwnerId(ownerId: Long): Long {
        return portfolioJpaRepository.countByOwnerId(ownerId)
    }

    override fun countByOrganizationId(organizationId: Long): Long {
        return portfolioJpaRepository.countByOrganizationId(organizationId)
    }

    // PortfolioQueryRepository implementation
    override fun findPortfolioSummary(id: Long): PortfolioSummary? {
        val portfolio = portfolioJpaRepository.findById(id).orElse(null) ?: return null
        val technologyCount = technologyJpaRepository.countByPortfolioId(id)
        val totalAnnualCost = calculateTotalAnnualCost(id)
        
        return portfolio.toSummary(technologyCount, totalAnnualCost)
    }

    override fun findPortfolioSummariesByOwner(ownerId: Long): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByOwnerId(ownerId)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    override fun findPortfolioSummariesByOrganization(organizationId: Long): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByOrganizationId(organizationId)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    override fun findPortfolioSummariesByType(type: PortfolioType): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByType(type)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    override fun findPortfolioSummariesByStatus(status: PortfolioStatus): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByStatus(status)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    override fun findAllPortfolioSummaries(): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByIsActiveTrue()
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    override fun searchPortfolios(name: String?, type: PortfolioType?, status: PortfolioStatus?, organizationId: Long?): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.searchPortfolios(name, type, status, organizationId)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    private fun calculateTotalAnnualCost(portfolioId: Long): BigDecimal {
        return technologyJpaRepository.findByPortfolioId(portfolioId)
            .mapNotNull { it.annualCost }
            .fold(BigDecimal.ZERO) { acc, cost -> acc.add(cost) }
    }

    // Extension functions for mapping
    private fun PortfolioEntity.toDomain(): TechnologyPortfolio {
        return TechnologyPortfolio(
            id = this.id,
            name = this.name,
            description = this.description,
            type = this.type,
            status = this.status,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            ownerId = this.ownerId,
            organizationId = this.organizationId
        )
    }

    private fun TechnologyPortfolio.toEntity(): PortfolioEntity {
        return PortfolioEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            type = this.type,
            status = this.status,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            ownerId = this.ownerId,
            organizationId = this.organizationId
        )
    }

    private fun PortfolioEntity.toSummary(technologyCount: Long, totalAnnualCost: BigDecimal): PortfolioSummary {
        return PortfolioSummary(
            id = this.id!!,
            name = this.name,
            type = this.type,
            status = this.status,
            ownerId = this.ownerId,
            organizationId = this.organizationId,
            technologyCount = technologyCount.toInt(),
            totalAnnualCost = totalAnnualCost,
            lastUpdated = this.updatedAt ?: this.createdAt
        )
    }
} 