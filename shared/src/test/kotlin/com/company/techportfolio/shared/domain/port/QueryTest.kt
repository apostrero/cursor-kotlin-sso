package com.company.techportfolio.shared.domain.port

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

// Test concrete implementation of Query
data class TestQuery(
    val queryData: String,
    val customQueryId: String? = null,
    val customTimestamp: LocalDateTime? = null
) : Query(
    customQueryId ?: java.util.UUID.randomUUID().toString(),
    customTimestamp ?: LocalDateTime.now()
)

class QueryTest {

    @Test
    fun `should create query with default values`() {
        val query = TestQuery("test data")

        assertEquals("test data", query.queryData)
        assertNotNull(query.queryId)
        assertNotNull(query.timestamp)
    }

    @Test
    fun `should create query with custom values`() {
        val customQueryId = "custom-query-id"
        val customTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val query = TestQuery(
            queryData = "custom test data",
            customQueryId = customQueryId,
            customTimestamp = customTimestamp
        )

        assertEquals("custom test data", query.queryData)
        assertEquals(customQueryId, query.queryId)
        assertEquals(customTimestamp, query.timestamp)
    }

    @Test
    fun `should generate unique query IDs`() {
        val query1 = TestQuery("data1")
        val query2 = TestQuery("data2")

        assertNotEquals(query1.queryId, query2.queryId)
    }

    @Test
    fun `should support data class equality for concrete queries`() {
        val queryId = "same-id"
        val timestamp = LocalDateTime.now()
        
        val query1 = TestQuery(
            queryData = "same data",
            customQueryId = queryId,
            customTimestamp = timestamp
        )
        
        val query2 = TestQuery(
            queryData = "same data",
            customQueryId = queryId,
            customTimestamp = timestamp
        )

        assertEquals(query1, query2)
        assertEquals(query1.hashCode(), query2.hashCode())
    }

    @Test
    fun `should support data class copy for concrete queries`() {
        val query = TestQuery(
            queryData = "original data",
            customQueryId = "test-id",
            customTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )
        val copiedQuery = query.copy(queryData = "modified data")

        assertEquals("modified data", copiedQuery.queryData)
        assertEquals("test-id", copiedQuery.customQueryId)
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0, 0), copiedQuery.customTimestamp)
    }
} 