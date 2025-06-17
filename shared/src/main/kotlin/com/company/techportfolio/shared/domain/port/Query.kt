package com.company.techportfolio.shared.domain.port

import java.time.LocalDateTime
import java.util.UUID

/**
 * Base abstract class for all queries in the CQRS pattern.
 * Queries represent operations that retrieve data from the system without changing state.
 */
abstract class Query(
    val queryId: String = UUID.randomUUID().toString(),
    val timestamp: LocalDateTime = LocalDateTime.now()
) 