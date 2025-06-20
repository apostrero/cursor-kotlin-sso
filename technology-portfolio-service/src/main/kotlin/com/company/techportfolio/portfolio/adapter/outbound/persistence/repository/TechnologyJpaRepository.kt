package com.company.techportfolio.portfolio.adapter.out.persistence.repository

import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Technology JPA Repository Interface
 * 
 * Spring Data JPA repository interface for TechnologyEntity persistence operations.
 * Extends JpaRepository to provide standard CRUD operations and defines custom
 * query methods for technology-specific data access patterns.
 * 
 * ## Repository Features:
 * - Standard CRUD operations via JpaRepository inheritance
 * - Custom finder methods using Spring Data naming conventions
 * - Advanced search with custom JPQL query
 * - Optimized queries for portfolio-based technology access
 * - Count operations for statistics and reporting
 * 
 * ## Query Methods:
 * - **Derived Queries**: Spring Data generates implementations from method names
 * - **Custom JPQL**: Complex search query with multiple optional parameters
 * - **Active Filtering**: Automatic filtering of inactive records
 * - **Parameter Binding**: Safe parameter binding to prevent SQL injection
 * 
 * ## Performance Considerations:
 * - Indexes recommended on: portfolio_id, name, category, type, vendor_name
 * - Search query uses LIKE for name/vendor matching (consider full-text search for large datasets)
 * - Active flag filtering reduces result set size
 * - Count operation is optimized for portfolio statistics
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see TechnologyEntity
 * @see JpaRepository
 * @see TechnologyType
 * @see MaturityLevel
 * @see RiskLevel
 */
@Repository
interface TechnologyJpaRepository : JpaRepository<TechnologyEntity, Long> {
    
    /**
     * Finds a technology by its name.
     * 
     * Technology names should be unique within a portfolio context.
     * This method is used for name-based lookups across the system.
     * 
     * @param name The name of the technology
     * @return The technology entity if found, null otherwise
     */
    fun findByName(name: String): TechnologyEntity?
    
    /**
     * Finds all technologies associated with a specific portfolio.
     * 
     * Returns all technologies that belong to the specified portfolio.
     * This is a core query for portfolio inventory management.
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return List of technology entities in the portfolio
     */
    fun findByPortfolioId(portfolioId: Long): List<TechnologyEntity>
    
    /**
     * Finds all technologies in a specific category.
     * 
     * Filters technologies by their category classification for
     * categorized reporting and management.
     * 
     * @param category The technology category to filter by
     * @return List of technology entities in the category
     */
    fun findByCategory(category: String): List<TechnologyEntity>
    
    /**
     * Finds all technologies of a specific type.
     * 
     * Filters technologies by their type classification for
     * type-based reporting and management.
     * 
     * @param type The technology type to filter by
     * @return List of technology entities of the specified type
     */
    fun findByType(type: TechnologyType): List<TechnologyEntity>
    
    /**
     * Finds all technologies from a specific vendor.
     * 
     * Filters technologies by their vendor or supplier for
     * vendor relationship management and consolidation reporting.
     * 
     * @param vendorName The vendor name to filter by
     * @return List of technology entities from the vendor
     */
    fun findByVendorName(vendorName: String): List<TechnologyEntity>
    
    /**
     * Finds all technologies with a specific maturity level.
     * 
     * Filters technologies by their maturity assessment for
     * maturity-based reporting and risk management.
     * 
     * @param maturityLevel The maturity level to filter by
     * @return List of technology entities with the specified maturity level
     */
    fun findByMaturityLevel(maturityLevel: MaturityLevel): List<TechnologyEntity>
    
    /**
     * Finds all technologies with a specific risk level.
     * 
     * Filters technologies by their risk assessment for
     * risk-based reporting and compliance management.
     * 
     * @param riskLevel The risk level to filter by
     * @return List of technology entities with the specified risk level
     */
    fun findByRiskLevel(riskLevel: RiskLevel): List<TechnologyEntity>
    
    /**
     * Finds all active technologies.
     * 
     * Returns only technologies with isActive = true, effectively
     * filtering out soft-deleted or inactive technologies.
     * 
     * @return List of active technology entities
     */
    fun findByIsActiveTrue(): List<TechnologyEntity>
    
    /**
     * Counts the number of technologies in a specific portfolio.
     * 
     * Provides technology count statistics for portfolio reporting
     * and management. Optimized count operation.
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return The number of technologies in the portfolio
     */
    fun countByPortfolioId(portfolioId: Long): Long

    /**
     * Searches technologies with flexible filtering criteria.
     * 
     * Advanced search method using custom JPQL query with multiple
     * optional parameters. All parameters are optional, allowing for
     * flexible search combinations. Uses LIKE operator for name and
     * vendor matching to support partial text searches.
     * 
     * ## Search Features:
     * - **Name Search**: Case-insensitive partial matching using LIKE
     * - **Category Filter**: Exact match on technology category
     * - **Type Filter**: Exact match on technology type
     * - **Vendor Search**: Case-insensitive partial matching using LIKE
     * - **Active Only**: Automatically filters to active technologies
     * - **Parameter Safety**: Uses @Param annotations for safe binding
     * 
     * ## Query Logic:
     * - All conditions are combined with AND
     * - NULL parameters are ignored (no filtering applied)
     * - Name and vendor searches use %text% pattern for substring matching
     * - Results include only active technologies (isActive = true)
     * 
     * ## Performance Notes:
     * - Consider database indexes on searchable fields
     * - LIKE operations may be slow on large datasets
     * - Consider full-text search for advanced text matching
     * 
     * @param name Optional name filter for partial matching (case-insensitive)
     * @param category Optional category filter (exact match)
     * @param type Optional technology type filter (exact match)
     * @param vendorName Optional vendor name filter for partial matching (case-insensitive)
     * @return List of technology entities matching the search criteria
     */
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