package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * JPA entity representing a technology portfolio.
 *
 * This entity maps to the "technology_portfolios" database table and represents
 * a collection of technologies managed as a portfolio. It includes portfolio
 * metadata, ownership information, and relationships to technologies and assessments.
 *
 * @property id Unique identifier for the portfolio (auto-generated)
 * @property name Name of the portfolio (required)
 * @property description Optional description of the portfolio
 * @property type Type/category of the portfolio
 * @property status Current status of the portfolio
 * @property isActive Whether the portfolio is currently active
 * @property createdAt Timestamp when the portfolio was created
 * @property updatedAt Timestamp when the portfolio was last updated
 * @property owner The user who owns this portfolio
 * @property organization Optional organization this portfolio belongs to
 * @property technologies Set of technologies in this portfolio
 * @property assessments Set of assessments for this portfolio
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Entity
@Table(name = "technology_portfolios")
data class TechnologyPortfolio(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Portfolio name is required")
    @field:Size(max = 200, message = "Portfolio name must not exceed 200 characters")
    @Column(nullable = false)
    val name: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column
    val description: String? = null,

    @field:NotNull(message = "Portfolio type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PortfolioType,

    @field:NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PortfolioStatus = PortfolioStatus.ACTIVE,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    val organization: Organization? = null,

    @OneToMany(mappedBy = "portfolio", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val technologies: Set<Technology> = emptySet(),

    @OneToMany(mappedBy = "portfolio", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val assessments: Set<PortfolioAssessment> = emptySet()
)

// Enums
enum class PortfolioType {
    ENTERPRISE, DEPARTMENTAL, PROJECT, PERSONAL
}

enum class PortfolioStatus {
    ACTIVE, INACTIVE, ARCHIVED, UNDER_REVIEW
}

enum class TechnologyType {
    SOFTWARE, HARDWARE, INFRASTRUCTURE, SERVICE, PLATFORM, TOOL, FRAMEWORK, LIBRARY
}

enum class MaturityLevel {
    EMERGING, GROWING, MATURE, DECLINING, LEGACY
}

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class AssessmentType {
    SECURITY, PERFORMANCE, COMPLIANCE, COST, TECHNICAL_DEBT, VENDOR_EVALUATION
}

enum class AssessmentStatus {
    PLANNED, IN_PROGRESS, COMPLETED, CANCELLED, OVERDUE
}

enum class DependencyType {
    REQUIRES, DEPENDS_ON, INTEGRATES_WITH, REPLACES, COMPETES_WITH
}

enum class DependencyStrength {
    WEAK, MODERATE, STRONG, CRITICAL
} 