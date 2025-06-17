package com.company.techportfolio.portfolio.adapter.out.persistence

import com.company.techportfolio.portfolio.adapter.out.persistence.repository.TechnologyJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.portfolio.domain.port.TechnologyRepository
import com.company.techportfolio.portfolio.domain.port.TechnologyQueryRepository
import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.portfolio.domain.model.TechnologySummary
import org.springframework.stereotype.Repository

@Repository
class TechnologyRepositoryAdapter(
    private val technologyJpaRepository: TechnologyJpaRepository
) : TechnologyRepository, TechnologyQueryRepository {

    // TechnologyRepository implementation
    override fun findById(id: Long): Technology? {
        return technologyJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByName(name: String): Technology? {
        return technologyJpaRepository.findByName(name)?.toDomain()
    }

    override fun findByPortfolioId(portfolioId: Long): List<Technology> {
        return technologyJpaRepository.findByPortfolioId(portfolioId).map { it.toDomain() }
    }

    override fun findByCategory(category: String): List<Technology> {
        return technologyJpaRepository.findByCategory(category).map { it.toDomain() }
    }

    override fun findByType(type: TechnologyType): List<Technology> {
        return technologyJpaRepository.findByType(type).map { it.toDomain() }
    }

    override fun findByVendor(vendorName: String): List<Technology> {
        return technologyJpaRepository.findByVendorName(vendorName).map { it.toDomain() }
    }

    override fun findByMaturityLevel(maturityLevel: com.company.techportfolio.shared.domain.model.MaturityLevel): List<Technology> {
        return technologyJpaRepository.findByMaturityLevel(maturityLevel).map { it.toDomain() }
    }

    override fun findByRiskLevel(riskLevel: com.company.techportfolio.shared.domain.model.RiskLevel): List<Technology> {
        return technologyJpaRepository.findByRiskLevel(riskLevel).map { it.toDomain() }
    }

    override fun save(technology: Technology): Technology {
        val entity = technology.toEntity()
        val savedEntity = technologyJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun update(technology: Technology): Technology {
        val entity = technology.toEntity()
        val savedEntity = technologyJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun delete(id: Long): Boolean {
        return try {
            technologyJpaRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun existsById(id: Long): Boolean {
        return technologyJpaRepository.existsById(id)
    }

    override fun countByPortfolioId(portfolioId: Long): Long {
        return technologyJpaRepository.countByPortfolioId(portfolioId)
    }

    // TechnologyQueryRepository implementation
    override fun findTechnologySummary(id: Long): TechnologySummary? {
        return technologyJpaRepository.findById(id).orElse(null)?.toSummary()
    }

    override fun findTechnologySummariesByPortfolio(portfolioId: Long): List<TechnologySummary> {
        return technologyJpaRepository.findByPortfolioId(portfolioId).map { it.toSummary() }
    }

    override fun findTechnologySummariesByCategory(category: String): List<TechnologySummary> {
        return technologyJpaRepository.findByCategory(category).map { it.toSummary() }
    }

    override fun findTechnologySummariesByType(type: TechnologyType): List<TechnologySummary> {
        return technologyJpaRepository.findByType(type).map { it.toSummary() }
    }

    override fun findTechnologySummariesByVendor(vendorName: String): List<TechnologySummary> {
        return technologyJpaRepository.findByVendorName(vendorName).map { it.toSummary() }
    }

    override fun findAllTechnologySummaries(): List<TechnologySummary> {
        return technologyJpaRepository.findByIsActiveTrue().map { it.toSummary() }
    }

    override fun searchTechnologies(name: String?, category: String?, type: TechnologyType?, vendorName: String?): List<TechnologySummary> {
        return technologyJpaRepository.searchTechnologies(name, category, type, vendorName).map { it.toSummary() }
    }

    private fun TechnologyEntity.toDomain(): Technology {
        return Technology(
            id = this.id,
            name = this.name,
            description = this.description,
            category = this.category,
            version = this.version,
            type = this.type,
            maturityLevel = this.maturityLevel,
            riskLevel = this.riskLevel,
            annualCost = this.annualCost,
            licenseCost = this.licenseCost,
            maintenanceCost = this.maintenanceCost,
            vendorName = this.vendorName,
            vendorContact = this.vendorContact,
            supportContractExpiry = this.supportContractExpiry,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            portfolioId = this.portfolioId
        )
    }

    private fun Technology.toEntity(): TechnologyEntity {
        return TechnologyEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            category = this.category,
            version = this.version,
            type = this.type,
            maturityLevel = this.maturityLevel,
            riskLevel = this.riskLevel,
            annualCost = this.annualCost,
            licenseCost = this.licenseCost,
            maintenanceCost = this.maintenanceCost,
            vendorName = this.vendorName,
            vendorContact = this.vendorContact,
            supportContractExpiry = this.supportContractExpiry,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            portfolioId = this.portfolioId
        )
    }

    private fun TechnologyEntity.toSummary(): TechnologySummary {
        return TechnologySummary(
            id = this.id!!,
            name = this.name,
            category = this.category,
            type = this.type,
            maturityLevel = this.maturityLevel,
            riskLevel = this.riskLevel,
            annualCost = this.annualCost,
            vendorName = this.vendorName
        )
    }
} 