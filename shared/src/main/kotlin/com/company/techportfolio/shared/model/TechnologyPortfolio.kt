package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

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

@Entity
@Table(name = "technologies")
data class Technology(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @field:NotBlank(message = "Technology name is required")
    @field:Size(max = 200, message = "Technology name must not exceed 200 characters")
    @Column(nullable = false)
    val name: String,
    
    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column
    val description: String? = null,
    
    @field:NotBlank(message = "Technology category is required")
    @field:Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(nullable = false)
    val category: String,
    
    @field:Size(max = 100, message = "Version must not exceed 100 characters")
    @Column
    val version: String? = null,
    
    @field:NotNull(message = "Technology type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TechnologyType,
    
    @field:NotNull(message = "Maturity level is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val maturityLevel: MaturityLevel,
    
    @field:NotNull(message = "Risk level is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val riskLevel: RiskLevel,
    
    @Column(name = "annual_cost", precision = 15, scale = 2)
    val annualCost: BigDecimal? = null,
    
    @Column(name = "license_cost", precision = 15, scale = 2)
    val licenseCost: BigDecimal? = null,
    
    @Column(name = "maintenance_cost", precision = 15, scale = 2)
    val maintenanceCost: BigDecimal? = null,
    
    @Column(name = "vendor_name", length = 200)
    val vendorName: String? = null,
    
    @Column(name = "vendor_contact", length = 200)
    val vendorContact: String? = null,
    
    @Column(name = "support_contract_expiry")
    val supportContractExpiry: LocalDateTime? = null,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    val portfolio: TechnologyPortfolio,
    
    @OneToMany(mappedBy = "technology", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val assessments: Set<TechnologyAssessment> = emptySet(),
    
    @OneToMany(mappedBy = "technology", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val dependencies: Set<TechnologyDependency> = emptySet()
)

@Entity
@Table(name = "portfolio_assessments")
data class PortfolioAssessment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @field:NotBlank(message = "Assessment title is required")
    @field:Size(max = 200, message = "Assessment title must not exceed 200 characters")
    @Column(nullable = false)
    val title: String,
    
    @field:Size(max = 1000, message = "Assessment description must not exceed 1000 characters")
    @Column
    val description: String? = null,
    
    @field:NotNull(message = "Assessment type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: AssessmentType,
    
    @field:NotNull(message = "Assessment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AssessmentStatus,
    
    @Column(name = "assessment_date", nullable = false)
    val assessmentDate: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "next_assessment_date")
    val nextAssessmentDate: LocalDateTime? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    val portfolio: TechnologyPortfolio,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessor_id", nullable = false)
    val assessor: User
)

@Entity
@Table(name = "technology_assessments")
data class TechnologyAssessment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @field:NotBlank(message = "Assessment title is required")
    @field:Size(max = 200, message = "Assessment title must not exceed 200 characters")
    @Column(nullable = false)
    val title: String,
    
    @field:Size(max = 1000, message = "Assessment description must not exceed 1000 characters")
    @Column
    val description: String? = null,
    
    @field:NotNull(message = "Assessment type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: AssessmentType,
    
    @field:NotNull(message = "Assessment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AssessmentStatus,
    
    @Column(name = "assessment_date", nullable = false)
    val assessmentDate: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "next_assessment_date")
    val nextAssessmentDate: LocalDateTime? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technology_id", nullable = false)
    val technology: Technology,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessor_id", nullable = false)
    val assessor: User
)

@Entity
@Table(name = "technology_dependencies")
data class TechnologyDependency(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @field:NotNull(message = "Dependency type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: DependencyType,
    
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Column
    val description: String? = null,
    
    @field:NotNull(message = "Dependency strength is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val strength: DependencyStrength,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technology_id", nullable = false)
    val technology: Technology,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_technology_id", nullable = false)
    val dependentTechnology: Technology
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