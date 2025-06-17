package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.portfolio.domain.model.TechnologySummary

interface TechnologyRepository {
    fun findById(id: Long): Technology?
    fun findByName(name: String): Technology?
    fun findByPortfolioId(portfolioId: Long): List<Technology>
    fun findByCategory(category: String): List<Technology>
    fun findByType(type: TechnologyType): List<Technology>
    fun findByVendor(vendorName: String): List<Technology>
    fun findByMaturityLevel(maturityLevel: com.company.techportfolio.shared.domain.model.MaturityLevel): List<Technology>
    fun findByRiskLevel(riskLevel: com.company.techportfolio.shared.domain.model.RiskLevel): List<Technology>
    fun save(technology: Technology): Technology
    fun update(technology: Technology): Technology
    fun delete(id: Long): Boolean
    fun existsById(id: Long): Boolean
    fun countByPortfolioId(portfolioId: Long): Long
} 