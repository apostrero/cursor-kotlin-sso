package com.company.techportfolio.shared.domain.event

/**
 * Event fired when a new assessment is created.
 */
data class AssessmentCreatedEvent(
    val assessmentId: Long,
    val assessmentType: String,
    val targetId: Long, // portfolio or technology id
    val targetType: String, // "portfolio" or "technology"
    val assessorId: Long
) : DomainEvent()

/**
 * Event fired when an assessment is completed.
 */
data class AssessmentCompletedEvent(
    val assessmentId: Long,
    val status: String,
    val completedBy: Long
) : DomainEvent() 