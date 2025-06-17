package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.portfolio.domain.model.TechnologySummary

interface TechnologyQueryRepository {
    fun findTechnologySummary(id: Long): TechnologySummary?
    fun findTechnologySummariesByPortfolio(portfolioId: Long): List<TechnologySummary>
    fun findTechnologySummariesByCategory(category: String): List<TechnologySummary>
    fun findTechnologySummariesByType(type: TechnologyType): List<TechnologySummary>
    fun findTechnologySummariesByVendor(vendorName: String): List<TechnologySummary>
    fun findAllTechnologySummaries(): List<TechnologySummary>
    fun searchTechnologies(name: String?, category: String?, type: TechnologyType?, vendorName: String?): List<TechnologySummary>
} 