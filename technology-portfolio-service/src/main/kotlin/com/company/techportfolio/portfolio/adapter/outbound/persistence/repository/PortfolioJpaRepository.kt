package com.company.techportfolio.portfolio.adapter.out.persistence.repository

import com.company.techportfolio.portfolio.adapter.out.persistence.entity.PortfolioEntity
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PortfolioJpaRepository : JpaRepository<PortfolioEntity, Long> {
    fun findByName(name: String): PortfolioEntity?
    fun findByOwnerId(ownerId: Long): List<PortfolioEntity>
    fun findByOrganizationId(organizationId: Long): List<PortfolioEntity>
    fun findByType(type: PortfolioType): List<PortfolioEntity>
    fun findByStatus(status: PortfolioStatus): List<PortfolioEntity>
    fun findByIsActiveTrue(): List<PortfolioEntity>
    fun countByOwnerId(ownerId: Long): Long
    fun countByOrganizationId(organizationId: Long): Long

    @Query("""
        SELECT p FROM PortfolioEntity p 
        WHERE (:name IS NULL OR p.name LIKE %:name%) 
        AND (:type IS NULL OR p.type = :type) 
        AND (:status IS NULL OR p.status = :status) 
        AND (:organizationId IS NULL OR p.organizationId = :organizationId)
        AND p.isActive = true
    """)
    fun searchPortfolios(
        @Param("name") name: String?,
        @Param("type") type: PortfolioType?,
        @Param("status") status: PortfolioStatus?,
        @Param("organizationId") organizationId: Long?
    ): List<PortfolioEntity>
} 