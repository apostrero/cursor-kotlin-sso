package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * JPA entity representing an organization in the system.
 * 
 * This entity maps to the "organizations" database table and represents
 * organizational units that can own portfolios and contain users. It supports
 * hierarchical organization structures through parent-child relationships.
 * 
 * @property id Unique identifier for the organization (auto-generated)
 * @property name Name of the organization (required)
 * @property description Optional description of the organization
 * @property isActive Whether the organization is currently active
 * @property createdAt Timestamp when the organization was created
 * @property updatedAt Timestamp when the organization was last updated
 * @property parentOrganization Optional parent organization for hierarchical structures
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Entity
@Table(name = "organizations")
data class Organization(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Organization name is required")
    @field:Size(max = 200, message = "Organization name must not exceed 200 characters")
    @Column(nullable = false)
    val name: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Column
    val description: String? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_organization_id")
    val parentOrganization: Organization? = null
) 