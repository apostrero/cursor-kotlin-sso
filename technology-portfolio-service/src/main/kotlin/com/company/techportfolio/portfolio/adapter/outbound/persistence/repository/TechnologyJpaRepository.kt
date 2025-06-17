package com.company.techportfolio.portfolio.adapter.out.persistence.repository

import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TechnologyJpaRepository : JpaRepository<TechnologyEntity, Long> {
    fun findByName(name: String): TechnologyEntity?
    fun findByPortfolioId(portfolioId: Long): List<TechnologyEntity>
    fun findByCategory(category: String): List<TechnologyEntity>
    fun findByType(type: TechnologyType): List<TechnologyEntity>
    fun findByVendorName(vendorName: String): List<TechnologyEntity>
    fun findByMaturityLevel(maturityLevel: MaturityLevel): List<TechnologyEntity>
    fun findByRiskLevel(riskLevel: RiskLevel): List<TechnologyEntity>
    fun findByIsActiveTrue(): List<TechnologyEntity>
    fun countByPortfolioId(portfolioId: Long): Long

    @Query("""
        SELECT t FROM TechnologyEntity t 
        WHERE (:name IS NULL OR t.name LIKE %:name%) 
        AND (:category IS NULL OR t.category = :category) 
        AND (:type IS NULL OR t.type = :type) 
        AND (:vendorName IS NULL OR t.vendorName LIKE %:vendorName%)
        AND t.isActive = true
    """)
    fun searchTechnologies(
        @Param("name") name: String?,
        @Param("category") category: String?,
        @Param("type") type: TechnologyType?,
        @Param("vendorName") vendorName: String?
    ): List<TechnologyEntity>
} 