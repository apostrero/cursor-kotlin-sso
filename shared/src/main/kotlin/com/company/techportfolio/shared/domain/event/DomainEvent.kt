package com.company.techportfolio.shared.domain.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime
import java.util.*

/**
 * Base abstract class for all domain events in the system.
 * Domain events represent something that happened in the domain that other parts of the system might be interested in.
 */
abstract class DomainEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val version: String = "1.0"
) {
    /**
     * The type of this event, automatically derived from the class name.
     */
    val eventType: String = this::class.java.simpleName

    /**
     * Converts the event to JSON string representation.
     *
     * @return JSON string representation of the event
     */
    fun toJson(): String {
        return ObjectMapper().writeValueAsString(this)
    }

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }
} 