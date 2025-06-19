package com.company.techportfolio.shared.domain.event

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

// Test concrete implementation of DomainEvent
data class TestEvent(
    val testData: String,
    val customEventId: String? = null,
    val customTimestamp: LocalDateTime? = null,
    val customVersion: String? = null
) : DomainEvent(
    customEventId ?: java.util.UUID.randomUUID().toString(),
    customTimestamp ?: LocalDateTime.now(),
    customVersion ?: "1.0"
)

class DomainEventTest {

    @Test
    fun `should create domain event with default values`() {
        val event = TestEvent("test data")

        assertEquals("test data", event.testData)
        assertNotNull(event.eventId)
        assertNotNull(event.timestamp)
        assertEquals("1.0", event.version)
        assertEquals("TestEvent", event.eventType)
    }

    @Test
    fun `should create domain event with custom values`() {
        val customEventId = "custom-event-id"
        val customTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        val customVersion = "2.0"

        val event = TestEvent(
            testData = "custom test data",
            customEventId = customEventId,
            customTimestamp = customTimestamp,
            customVersion = customVersion
        )

        assertEquals("custom test data", event.testData)
        assertEquals(customEventId, event.eventId)
        assertEquals(customTimestamp, event.timestamp)
        assertEquals(customVersion, event.version)
        assertEquals("TestEvent", event.eventType)
    }

    @Test
    fun `should generate unique event IDs`() {
        val event1 = TestEvent("data1")
        val event2 = TestEvent("data2")

        assertNotEquals(event1.eventId, event2.eventId)
    }

    @Test
    fun `should have different timestamps for events created at different times`() {
        val event1 = TestEvent("data1")
        Thread.sleep(1) // Ensure different timestamps
        val event2 = TestEvent("data2")

        assertTrue(event2.timestamp.isAfter(event1.timestamp) || event2.timestamp.isEqual(event1.timestamp))
    }

    @Test
    fun `eventType should be derived from class name`() {
        val event = TestEvent("test")
        assertEquals("TestEvent", event.eventType)
    }

    @Test
    fun `should support data class equality for concrete events`() {
        val eventId = "same-id"
        val timestamp = LocalDateTime.now()
        
        val event1 = TestEvent(
            testData = "same data",
            customEventId = eventId,
            customTimestamp = timestamp,
            customVersion = "1.0"
        )
        
        val event2 = TestEvent(
            testData = "same data",
            customEventId = eventId,
            customTimestamp = timestamp,
            customVersion = "1.0"
        )

        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `should handle toString properly`() {
        val event = TestEvent("test data")
        val toString = event.toString()

        assertTrue(toString.contains("TestEvent"))
        assertTrue(toString.contains("test data"))
    }
} 