package com.company.techportfolio.shared.domain.port

import com.company.techportfolio.shared.domain.model.TechnologyPortfolio
import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.PortfolioAssessment
import com.company.techportfolio.shared.domain.model.TechnologyAssessment
import com.company.techportfolio.shared.domain.model.TechnologyDependency

interface TechnologyPortfolioRepository {
    fun findById(id: Long): TechnologyPortfolio?
    fun findByName(name: String): TechnologyPortfolio?
    fun findByOwnerId(ownerId: Long): List<TechnologyPortfolio>
    fun findByOrganizationId(organizationId: Long): List<TechnologyPortfolio>
    fun save(portfolio: TechnologyPortfolio): TechnologyPortfolio
    fun update(portfolio: TechnologyPortfolio): TechnologyPortfolio
    fun delete(id: Long): Boolean
    fun findAll(): List<TechnologyPortfolio>
    fun findByType(type: com.company.techportfolio.shared.domain.model.PortfolioType): List<TechnologyPortfolio>
    fun findByStatus(status: com.company.techportfolio.shared.domain.model.PortfolioStatus): List<TechnologyPortfolio>
}

interface TechnologyRepository {
    fun findById(id: Long): Technology?
    fun findByName(name: String): Technology?
    fun findByPortfolioId(portfolioId: Long): List<Technology>
    fun findByCategory(category: String): List<Technology>
    fun findByType(type: com.company.techportfolio.shared.domain.model.TechnologyType): List<Technology>
    fun findByVendor(vendorName: String): List<Technology>
    fun save(technology: Technology): Technology
    fun update(technology: Technology): Technology
    fun delete(id: Long): Boolean
    fun findAll(): List<Technology>
}

interface PortfolioAssessmentRepository {
    fun findById(id: Long): PortfolioAssessment?
    fun findByPortfolioId(portfolioId: Long): List<PortfolioAssessment>
    fun findByAssessorId(assessorId: Long): List<PortfolioAssessment>
    fun findByType(type: com.company.techportfolio.shared.domain.model.AssessmentType): List<PortfolioAssessment>
    fun findByStatus(status: com.company.techportfolio.shared.domain.model.AssessmentStatus): List<PortfolioAssessment>
    fun save(assessment: PortfolioAssessment): PortfolioAssessment
    fun update(assessment: PortfolioAssessment): PortfolioAssessment
    fun delete(id: Long): Boolean
    fun findAll(): List<PortfolioAssessment>
}

interface TechnologyAssessmentRepository {
    fun findById(id: Long): TechnologyAssessment?
    fun findByTechnologyId(technologyId: Long): List<TechnologyAssessment>
    fun findByAssessorId(assessorId: Long): List<TechnologyAssessment>
    fun findByType(type: com.company.techportfolio.shared.domain.model.AssessmentType): List<TechnologyAssessment>
    fun findByStatus(status: com.company.techportfolio.shared.domain.model.AssessmentStatus): List<TechnologyAssessment>
    fun save(assessment: TechnologyAssessment): TechnologyAssessment
    fun update(assessment: TechnologyAssessment): TechnologyAssessment
    fun delete(id: Long): Boolean
    fun findAll(): List<TechnologyAssessment>
}

interface TechnologyDependencyRepository {
    fun findById(id: Long): TechnologyDependency?
    fun findByTechnologyId(technologyId: Long): List<TechnologyDependency>
    fun findByDependentTechnologyId(dependentTechnologyId: Long): List<TechnologyDependency>
    fun findByType(type: com.company.techportfolio.shared.domain.model.DependencyType): List<TechnologyDependency>
    fun save(dependency: TechnologyDependency): TechnologyDependency
    fun update(dependency: TechnologyDependency): TechnologyDependency
    fun delete(id: Long): Boolean
    fun findAll(): List<TechnologyDependency>
} 