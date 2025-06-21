package com.company.techportfolio.portfolio.adapter.out.persistence.entity

import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.PortfolioType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * Portfolio Entity - R2DBC Database Mapping
 *
 * This entity class represents the database mapping for technology portfolios
 * in the persistence layer. It uses R2DBC annotations to define the table
 * structure, constraints, and relationships for reactive portfolio data storage.
 *
 * ## Database Mapping:
 * - **Table**: `portfolios`
 * - **Primary Key**: Auto-generated Long ID
 * - **Unique Constraints**: Portfolio name must be unique
 * - **Indexes**: Recommended on owner_id, organization_id, status, type
 *
 * ## Business Rules Enforced:
 * - Portfolio names must be unique across the system
 * - Owner ID is required (cannot be null)
 * - Portfolio type and status are required
 * - Active flag defaults to true for new portfolios
 * - Created timestamp is required and immutable
 * - Updated timestamp is optional and mutable
 *
 * ## Relationships:
 * - Belongs to a User (owner_id foreign key)
 * - Optionally belongs to an Organization (organization_id foreign key)
 * - Has many Technologies (one-to-many relationship)
 *
 * ## Reactive Features:
 * - Non-blocking database operations
 * - Reactive stream support
 * - Optimized for reactive repositories
 *
 * @property id Unique identifier (auto-generated primary key)
 * @property name Portfolio name (unique, required, max length varies by DB)
 * @property description Optional portfolio description (TEXT field)
 * @property type Portfolio type classification (enum stored as string)
 * @property status Current portfolio status (enum stored as string)
 * @property isActive Active flag for soft deletion and filtering
 * @property createdAt Timestamp when portfolio was created (immutable)
 * @property updatedAt Timestamp when portfolio was last updated (nullable)
 * @property ownerId Foreign key to the owning user (required)
 * @property organizationId Optional foreign key to organization (nullable)
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioType
 * @see PortfolioStatus
 */
@Table("portfolios")
data class PortfolioEntity(
    /**
     * Unique identifier for the portfolio.
     *
     * Auto-generated primary key using database identity strategy.
     * Null for new entities before persistence.
     */
    @Id
    val id: Long? = null,

    /**
     * Name of the portfolio.
     *
     * Must be unique across the entire system to prevent confusion.
     * Required field that serves as the primary human-readable identifier.
     * Database constraint enforces uniqueness.
     */
    @Column("name")
    val name: String,

    /**
     * Optional description of the portfolio.
     *
     * Stored as TEXT column to support longer descriptions.
     * Can be null for portfolios without detailed descriptions.
     */
    @Column("description")
    val description: String? = null,

    /**
     * Type classification of the portfolio.
     *
     * Enum value stored as string in the database for readability.
     * Required field that categorizes the portfolio's purpose and scope.
     */
    @Column("type")
    val type: PortfolioType,

    /**
     * Current status of the portfolio.
     *
     * Enum value stored as string in the database for readability.
     * Required field that indicates the portfolio's lifecycle state.
     */
    @Column("status")
    val status: PortfolioStatus,

    /**
     * Active flag for the portfolio.
     *
     * Used for soft deletion and filtering. Defaults to true.
     * Inactive portfolios are typically hidden from normal operations.
     */
    @Column("is_active")
    val isActive: Boolean = true,

    /**
     * Timestamp when the portfolio was created.
     *
     * Immutable field set once during initial creation.
     * Required field for audit trail and lifecycle tracking.
     */
    @Column("created_at")
    val createdAt: LocalDateTime,

    /**
     * Timestamp when the portfolio was last updated.
     *
     * Nullable field that gets updated on each modification.
     * Null for portfolios that have never been updated after creation.
     */
    @Column("updated_at")
    val updatedAt: LocalDateTime? = null,

    /**
     * Foreign key to the owning user.
     *
     * Required field that establishes ownership relationship.
     * References the user ID from the user management system.
     */
    @Column("owner_id")
    val ownerId: Long,

    /**
     * Optional foreign key to the organization.
     *
     * Nullable field for multi-tenant organizational structure.
     * Personal portfolios may not belong to any organization.
     */
    @Column("organization_id")
    val organizationId: Long? = null
)