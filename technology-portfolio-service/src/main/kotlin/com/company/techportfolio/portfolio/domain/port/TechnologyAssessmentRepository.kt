package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.model.AssessmentStatus
import com.company.techportfolio.shared.model.AssessmentType
import com.company.techportfolio.shared.model.TechnologyAssessment

/**
 * Repository interface for technology assessment persistence operations.
 *
 * This interface defines the contract for data access operations related to
 * technology assessments within the portfolio service.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface TechnologyAssessmentRepository {
    fun findById(id: Long): TechnologyAssessment?
    fun findByTechnologyId(technologyId: Long): List<TechnologyAssessment>
    fun findByAssessorId(assessorId: Long): List<TechnologyAssessment>
    fun findByType(type: AssessmentType): List<TechnologyAssessment>
    fun findByStatus(status: AssessmentStatus): List<TechnologyAssessment>
    fun save(assessment: TechnologyAssessment): TechnologyAssessment
    fun update(assessment: TechnologyAssessment): TechnologyAssessment
    fun delete(id: Long): Boolean
    fun findAll(): List<TechnologyAssessment>
} 