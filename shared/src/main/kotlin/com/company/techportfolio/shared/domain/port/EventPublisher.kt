package com.company.techportfolio.shared.domain.port

import com.company.techportfolio.shared.domain.event.DomainEvent

interface EventPublisher {
    fun publish(event: DomainEvent)
    fun publishAll(events: List<DomainEvent>)
}

interface EventHandler<T : com.company.techportfolio.shared.domain.event.DomainEvent> {
    fun handle(event: T)
}

interface EventStore {
    fun save(event: com.company.techportfolio.shared.domain.event.DomainEvent)
    fun saveAll(events: List<com.company.techportfolio.shared.domain.event.DomainEvent>)
    fun getEvents(aggregateId: String): List<com.company.techportfolio.shared.domain.event.DomainEvent>
    fun getEventsByType(eventType: String): List<com.company.techportfolio.shared.domain.event.DomainEvent>
} 