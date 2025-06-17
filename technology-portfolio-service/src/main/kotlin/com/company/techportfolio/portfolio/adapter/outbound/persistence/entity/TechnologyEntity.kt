package com.company.techportfolio.portfolio.adapter.out.persistence.entity

import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "technologies")
class TechnologyEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "category", nullable = false)
    val category: String,

    @Column(name = "version")
    val version: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: TechnologyType,

    @Enumerated(EnumType.STRING)
    @Column(name = "maturity_level", nullable = false)
    val maturityLevel: MaturityLevel,

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    val riskLevel: RiskLevel,

    @Column(name = "annual_cost", precision = 15, scale = 2)
    val annualCost: BigDecimal? = null,

    @Column(name = "license_cost", precision = 15, scale = 2)
    val licenseCost: BigDecimal? = null,

    @Column(name = "maintenance_cost", precision = 15, scale = 2)
    val maintenanceCost: BigDecimal? = null,

    @Column(name = "vendor_name")
    val vendorName: String? = null,

    @Column(name = "vendor_contact")
    val vendorContact: String? = null,

    @Column(name = "support_contract_expiry")
    val supportContractExpiry: LocalDateTime? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "portfolio_id", nullable = false)
    val portfolioId: Long
) 