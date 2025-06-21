package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.model.AssessmentStatus
import com.company.techportfolio.shared.model.AssessmentType
import com.company.techportfolio.shared.model.PortfolioAssessment

/**
 * Repository interface for portfolio assessment persistence operations.
 *
 * This interface defines the contract for data access operations related to
 * portfolio assessments within the portfolio service.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface PortfolioAssessmentRepository {
    fun findById(id: Long): PortfolioAssessment?
    fun findByPortfolioId(portfolioId: Long): List<PortfolioAssessment>
    fun findByAssessorId(assessorId: Long): List<PortfolioAssessment>
    fun findByType(type: AssessmentType): List<PortfolioAssessment>
    fun findByStatus(status: AssessmentStatus): List<PortfolioAssessment>
    fun save(assessment: PortfolioAssessment): PortfolioAssessment
    fun update(assessment: PortfolioAssessment): PortfolioAssessment
    fun delete(id: Long): Boolean
    fun findAll(): List<PortfolioAssessment>
} 