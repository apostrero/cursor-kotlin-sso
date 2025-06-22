package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.model.DependencyType
import com.company.techportfolio.shared.model.TechnologyDependency

/**
 * Repository interface for technology dependency persistence operations.
 *
 * This interface defines the contract for data access operations related to
 * technology dependencies within the portfolio service.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface TechnologyDependencyRepository {
    fun findById(id: Long): TechnologyDependency?
    fun findByTechnologyId(technologyId: Long): List<TechnologyDependency>
    fun findByDependentTechnologyId(dependentTechnologyId: Long): List<TechnologyDependency>
    fun findByType(type: DependencyType): List<TechnologyDependency>
    fun save(dependency: TechnologyDependency): TechnologyDependency
    fun update(dependency: TechnologyDependency): TechnologyDependency
    fun delete(id: Long): Boolean
    fun findAll(): List<TechnologyDependency>
} 