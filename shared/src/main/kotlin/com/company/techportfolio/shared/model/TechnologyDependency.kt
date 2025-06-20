package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * JPA entity representing a dependency between technologies.
 * 
 * This entity maps to the "technology_dependencies" database table and represents
 * relationships and dependencies between different technologies within portfolios.
 * It helps track how technologies interact, depend on each other, or compete.
 * 
 * @property id Unique identifier for the dependency (auto-generated)
 * @property type Type of dependency relationship
 * @property description Optional description of the dependency
 * @property strength Strength level of the dependency
 * @property createdAt Timestamp when the dependency was created
 * @property technology The source technology in the dependency relationship
 * @property dependentTechnology The target technology in the dependency relationship
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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