package com.company.techportfolio.shared.domain.model

import java.time.LocalDateTime

data class TechnologyAssessment(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val type: AssessmentType,
    val status: AssessmentStatus,
    val assessmentDate: LocalDateTime = LocalDateTime.now(),
    val nextAssessmentDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val technologyId: Long,
    val assessorId: Long
) 