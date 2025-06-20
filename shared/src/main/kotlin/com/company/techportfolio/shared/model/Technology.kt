package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * JPA entity representing a technology within a portfolio.
 * 
 * This entity maps to the "technologies" database table and represents individual
 * technologies that are part of technology portfolios. It includes comprehensive
 * technology information including technical details, cost information, vendor
 * details, and risk assessments.
 * 
 * @property id Unique identifier for the technology (auto-generated)
 * @property name Name of the technology (required)
 * @property description Optional description of the technology
 * @property category Category this technology belongs to (required)
 * @property version Optional version information
 * @property type Type classification of the technology
 * @property maturityLevel Maturity level assessment
 * @property riskLevel Risk level assessment
 * @property annualCost Optional annual cost of the technology
 * @property licenseCost Optional licensing cost
 * @property maintenanceCost Optional maintenance cost
 * @property vendorName Optional vendor/supplier name
 * @property vendorContact Optional vendor contact information
 * @property supportContractExpiry Optional support contract expiration date
 * @property isActive Whether the technology is currently active
 * @property createdAt Timestamp when the technology was created
 * @property updatedAt Timestamp when the technology was last updated
 * @property portfolio The portfolio this technology belongs to
 * @property assessments Set of assessments for this technology
 * @property dependencies Set of dependencies for this technology
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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