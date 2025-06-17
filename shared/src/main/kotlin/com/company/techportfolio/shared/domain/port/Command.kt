package com.company.techportfolio.shared.domain.port

import java.time.LocalDateTime
import java.util.UUID

/**
 * Base abstract class for all commands in the CQRS pattern.
 * Commands represent operations that change the state of the system.
 */
abstract class Command(
    val commandId: String = UUID.randomUUID().toString(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val version: String = "1.0"
) 