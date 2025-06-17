package com.company.techportfolio.portfolio.adapter.out.persistence.entity

import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "portfolios")
class PortfolioEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false, unique = true)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: PortfolioType,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: PortfolioStatus,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "owner_id", nullable = false)
    val ownerId: Long,

    @Column(name = "organization_id")
    val organizationId: Long? = null
) 