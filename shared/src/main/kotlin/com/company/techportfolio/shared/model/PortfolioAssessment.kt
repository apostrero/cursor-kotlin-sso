package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * JPA entity representing a portfolio assessment.
 * 
 * This entity maps to the "portfolio_assessments" database table and represents
 * assessments conducted on technology portfolios. It includes assessment metadata,
 * status tracking, and relationships to portfolios and assessors.
 * 
 * @property id Unique identifier for the assessment (auto-generated)
 * @property title Title of the assessment (required)
 * @property description Optional description of the assessment
 * @property type Type of assessment being conducted
 * @property status Current status of the assessment
 * @property assessmentDate When the assessment was conducted
 * @property nextAssessmentDate When the next assessment is scheduled
 * @property createdAt Timestamp when the assessment was created
 * @property portfolio The portfolio being assessed
 * @property assessor The user conducting the assessment
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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