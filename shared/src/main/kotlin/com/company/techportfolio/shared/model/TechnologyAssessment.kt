package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * JPA entity representing a technology assessment.
 *
 * This entity maps to the "technology_assessments" database table and represents
 * assessments conducted on individual technologies within portfolios. It includes
 * assessment metadata, status tracking, and relationships to technologies and assessors.
 *
 * @property id Unique identifier for the assessment (auto-generated)
 * @property title Title of the assessment (required)
 * @property description Optional description of the assessment
 * @property type Type of assessment being conducted
 * @property status Current status of the assessment
 * @property assessmentDate When the assessment was conducted
 * @property nextAssessmentDate When the next assessment is scheduled
 * @property createdAt Timestamp when the assessment was created
 * @property technology The technology being assessed
 * @property assessor The user conducting the assessment
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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