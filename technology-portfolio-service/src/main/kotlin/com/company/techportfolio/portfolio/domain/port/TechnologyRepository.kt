package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.portfolio.domain.model.TechnologySummary

/**
 * Technology Repository Port - Domain Interface
 * 
 * This interface defines the contract for technology data access operations
 * within the hexagonal architecture. It represents the domain's requirements
 * for technology persistence and querying without coupling to specific
 * implementation details.
 * 
 * ## Responsibilities:
 * - Technology entity CRUD operations
 * - Technology querying and filtering by various criteria
 * - Portfolio association management
 * - Cost and vendor information tracking
 * - Risk and maturity assessment data access
 * 
 * ## Implementation Notes:
 * - Implementations should handle data validation
 * - Database constraints should enforce referential integrity
 * - Null returns indicate entity not found
 * - Exceptions should be thrown for constraint violations
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see Technology
 * @see TechnologyType
 * @see TechnologySummary
 */
interface TechnologyRepository {
    
    /**
     * Finds a technology by its unique identifier.
     * 
     * @param id The unique identifier of the technology
     * @return The technology if found, null otherwise
     */
    fun findById(id: Long): Technology?
    
    /**
     * Finds a technology by its name.
     * 
     * Technology names should be unique within a portfolio context
     * to prevent confusion and ensure clear identification.
     * 
     * @param name The name of the technology
     * @return The technology if found, null otherwise
     */
    fun findByName(name: String): Technology?
    
    /**
     * Finds all technologies associated with a specific portfolio.
     * 
     * Returns all technologies that belong to the specified portfolio,
     * supporting portfolio inventory management and reporting.
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return List of technologies in the portfolio (empty if none found)
     */
    fun findByPortfolioId(portfolioId: Long): List<Technology>
    
    /**
     * Finds all technologies in a specific category.
     * 
     * Allows filtering technologies by their category classification
     * (e.g., "Framework", "Database", "Cloud Service").
     * 
     * @param category The technology category to filter by
     * @return List of technologies in the category (empty if none found)
     */
    fun findByCategory(category: String): List<Technology>
    
    /**
     * Finds all technologies of a specific type.
     * 
     * Allows filtering technologies by their type classification
     * (e.g., FRAMEWORK, DATABASE, CLOUD_SERVICE, TOOL).
     * 
     * @param type The technology type to filter by
     * @return List of technologies of the specified type (empty if none found)
     */
    fun findByType(type: TechnologyType): List<Technology>
    
    /**
     * Finds all technologies from a specific vendor.
     * 
     * Allows filtering technologies by their vendor or supplier,
     * supporting vendor management and relationship tracking.
     * 
     * @param vendorName The vendor name to filter by
     * @return List of technologies from the vendor (empty if none found)
     */
    fun findByVendor(vendorName: String): List<Technology>
    
    /**
     * Finds all technologies with a specific maturity level.
     * 
     * Allows filtering technologies by their maturity assessment
     * (e.g., EXPERIMENTAL, EMERGING, MATURE, LEGACY).
     * 
     * @param maturityLevel The maturity level to filter by
     * @return List of technologies with the specified maturity level (empty if none found)
     */
    fun findByMaturityLevel(maturityLevel: com.company.techportfolio.shared.domain.model.MaturityLevel): List<Technology>
    
    /**
     * Finds all technologies with a specific risk level.
     * 
     * Allows filtering technologies by their risk assessment
     * (e.g., LOW, MEDIUM, HIGH, CRITICAL).
     * 
     * @param riskLevel The risk level to filter by
     * @return List of technologies with the specified risk level (empty if none found)
     */
    fun findByRiskLevel(riskLevel: com.company.techportfolio.shared.domain.model.RiskLevel): List<Technology>
    
    /**
     * Saves a new technology to the repository.
     * 
     * Creates a new technology entity with generated ID and timestamps.
     * The technology must be associated with a valid portfolio.
     * 
     * @param technology The technology entity to save (ID should be null for new entities)
     * @return The saved technology with generated ID and timestamps
     * @throws IllegalArgumentException if associated portfolio doesn't exist
     * @throws RuntimeException if save operation fails
     */
    fun save(technology: Technology): Technology
    
    /**
     * Updates an existing technology in the repository.
     * 
     * Updates the technology entity with new values and updated timestamp.
     * The technology must exist before updating.
     * 
     * @param technology The technology entity to update (ID must not be null)
     * @return The updated technology with new timestamp
     * @throws IllegalArgumentException if technology doesn't exist
     * @throws RuntimeException if update operation fails
     */
    fun update(technology: Technology): Technology
    
    /**
     * Deletes a technology by its unique identifier.
     * 
     * Permanently removes the technology from the repository.
     * This operation should be used carefully as it cannot be undone.
     * 
     * @param id The unique identifier of the technology to delete
     * @return true if the technology was deleted, false if not found
     * @throws RuntimeException if deletion fails due to constraints
     */
    fun delete(id: Long): Boolean
    
    /**
     * Checks if a technology exists by its unique identifier.
     * 
     * Provides a lightweight way to verify technology existence
     * without retrieving the full entity.
     * 
     * @param id The unique identifier of the technology
     * @return true if the technology exists, false otherwise
     */
    fun existsById(id: Long): Boolean
    
    /**
     * Counts the number of technologies in a specific portfolio.
     * 
     * Provides technology count statistics for portfolio reporting
     * and management purposes.
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return The number of technologies in the portfolio
     */
    fun countByPortfolioId(portfolioId: Long): Long
} 