package com.company.techportfolio.portfolio.adapter.out.persistence.entity

import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.company.techportfolio.shared.domain.model.TechnologyType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Technology Entity - R2DBC Database Mapping
 *
 * This entity class represents the database mapping for technologies within
 * portfolios in the persistence layer. It uses R2DBC annotations to define the
 * table structure, constraints, and relationships for reactive technology
 * data storage including cost tracking and vendor management.
 *
 * ## Database Mapping:
 * - **Table**: `technologies`
 * - **Primary Key**: Auto-generated Long ID
 * - **Foreign Key**: portfolio_id references portfolios table
 * - **Indexes**: Recommended on portfolio_id, category, type, vendor_name
 *
 * ## Business Rules Enforced:
 * - Technology name is required
 * - Category classification is required
 * - Technology type and risk/maturity levels are required
 * - Portfolio association is mandatory (foreign key)
 * - Cost fields use precision 15, scale 2 for financial accuracy
 * - Active flag defaults to true for new technologies
 * - Created timestamp is required and immutable
 *
 * ## Cost Management:
 * - Annual, license, and maintenance costs tracked separately
 * - BigDecimal used for precise financial calculations
 * - All cost fields are optional to support incomplete data
 *
 * ## Vendor Information:
 * - Vendor name and contact information tracking
 * - Support contract expiry date management
 * - Optional fields to support various technology sources
 *
 * ## Reactive Features:
 * - Non-blocking database operations
 * - Reactive stream support
 * - Optimized for reactive repositories
 *
 * @property id Unique identifier (auto-generated primary key)
 * @property name Technology name (required)
 * @property description Optional detailed description (TEXT field)
 * @property category Technology category classification (required)
 * @property version Optional version information
 * @property type Technology type enum (stored as string)
 * @property maturityLevel Maturity assessment enum (stored as string)
 * @property riskLevel Risk assessment enum (stored as string)
 * @property annualCost Optional annual cost (precision 15, scale 2)
 * @property licenseCost Optional licensing cost (precision 15, scale 2)
 * @property maintenanceCost Optional maintenance cost (precision 15, scale 2)
 * @property vendorName Optional vendor/supplier name
 * @property vendorContact Optional vendor contact information
 * @property supportContractExpiry Optional support contract expiration
 * @property isActive Active flag for soft deletion and filtering
 * @property createdAt Timestamp when technology was created (immutable)
 * @property updatedAt Timestamp when technology was last updated (nullable)
 * @property portfolioId Foreign key to the owning portfolio (required)
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see TechnologyType
 * @see MaturityLevel
 * @see RiskLevel
 */
@Table("technologies")
data class TechnologyEntity(
    /**
     * Unique identifier for the technology.
     *
     * Auto-generated primary key using database identity strategy.
     * Null for new entities before persistence.
     */
    @Id
    val id: Long? = null,

    /**
     * Name of the technology.
     *
     * Required field that serves as the primary identifier.
     * Should be descriptive and recognizable by users.
     */
    @Column("name")
    val name: String,

    /**
     * Optional detailed description of the technology.
     *
     * Stored as TEXT column to support longer descriptions.
     * Can include purpose, features, and usage information.
     */
    @Column("description")
    val description: String? = null,

    /**
     * Technology category classification.
     *
     * Required field for organizing and filtering technologies.
     * Examples: "Framework", "Database", "Cloud Service", "Tool".
     */
    @Column("category")
    val category: String,

    /**
     * Optional version information.
     *
     * Can store version numbers, release names, or other
     * version identifiers for technology lifecycle tracking.
     */
    @Column("version")
    val version: String? = null,

    /**
     * Technology type classification.
     *
     * Enum value stored as string for standardized categorization.
     * Required field for technology taxonomy and reporting.
     */
    @Column("type")
    val type: TechnologyType,

    /**
     * Maturity level assessment.
     *
     * Enum value indicating the technology's maturity stage.
     * Used for risk assessment and adoption decisions.
     */
    @Column("maturity_level")
    val maturityLevel: MaturityLevel,

    /**
     * Risk level assessment.
     *
     * Enum value indicating the associated risk level.
     * Critical for compliance and risk management reporting.
     */
    @Column("risk_level")
    val riskLevel: RiskLevel,

    /**
     * Optional annual cost of the technology.
     *
     * BigDecimal with precision 15, scale 2 for financial accuracy.
     * Includes all recurring annual expenses for this technology.
     */
    @Column("annual_cost")
    val annualCost: BigDecimal? = null,

    /**
     * Optional licensing cost.
     *
     * BigDecimal for precise financial tracking of license expenses.
     * May be one-time or recurring depending on license model.
     */
    @Column("license_cost")
    val licenseCost: BigDecimal? = null,

    /**
     * Optional maintenance cost.
     *
     * BigDecimal for tracking ongoing maintenance and support costs.
     * Separate from licensing for detailed cost analysis.
     */
    @Column("maintenance_cost")
    val maintenanceCost: BigDecimal? = null,

    /**
     * Optional vendor or supplier name.
     *
     * Tracks the primary vendor for vendor relationship management
     * and consolidation reporting.
     */
    @Column("vendor_name")
    val vendorName: String? = null,

    /**
     * Optional vendor contact information.
     *
     * Can store email, phone, or other contact details
     * for vendor relationship management.
     */
    @Column("vendor_contact")
    val vendorContact: String? = null,

    /**
     * Optional support contract expiration date.
     *
     * Tracks when support contracts expire for proactive
     * renewal management and risk mitigation.
     */
    @Column("support_contract_expiry")
    val supportContractExpiry: LocalDateTime? = null,

    /**
     * Active flag for the technology.
     *
     * Used for soft deletion and filtering. Defaults to true.
     * Inactive technologies are typically hidden from normal operations.
     */
    @Column("is_active")
    val isActive: Boolean = true,

    /**
     * Timestamp when the technology was created.
     *
     * Immutable field set once during initial creation.
     * Required field for audit trail and lifecycle tracking.
     */
    @Column("created_at")
    val createdAt: LocalDateTime,

    /**
     * Timestamp when the technology was last updated.
     *
     * Nullable field that gets updated on each modification.
     * Null for technologies that have never been updated after creation.
     */
    @Column("updated_at")
    val updatedAt: LocalDateTime? = null,

    /**
     * Foreign key to the owning portfolio.
     *
     * Required field that establishes the portfolio relationship.
     * References the portfolio ID from the portfolios table.
     */
    @Column("portfolio_id")
    val portfolioId: Long
) 