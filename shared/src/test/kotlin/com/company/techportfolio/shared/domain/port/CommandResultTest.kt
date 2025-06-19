package com.company.techportfolio.shared.domain.port

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CommandResultTest {

    @Test
    fun `should create CommandResult with all parameters`() {
        val data = mapOf("key" to "value")
        val errors = listOf("error1", "error2")
        
        val result = CommandResult(
            success = true,
            message = "Operation completed successfully",
            data = data,
            errors = errors
        )

        assertTrue(result.success)
        assertEquals("Operation completed successfully", result.message)
        assertEquals(data, result.data)
        assertEquals(errors, result.errors)
    }

    @Test
    fun `should create CommandResult with default values`() {
        val result = CommandResult(
            success = false,
            message = "Operation failed"
        )

        assertFalse(result.success)
        assertEquals("Operation failed", result.message)
        assertNull(result.data)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `should create successful result using companion object method`() {
        val result = CommandResult.success("Operation completed")

        assertTrue(result.success)
        assertEquals("Operation completed", result.message)
        assertNull(result.data)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `should create successful result with data using companion object method`() {
        val data = mapOf("id" to 123, "name" to "test")
        val result = CommandResult.success("User created", data)

        assertTrue(result.success)
        assertEquals("User created", result.message)
        assertEquals(data, result.data)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `should create failure result using companion object method`() {
        val result = CommandResult.failure("Operation failed")

        assertFalse(result.success)
        assertEquals("Operation failed", result.message)
        assertNull(result.data)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `should create failure result with errors using companion object method`() {
        val errors = listOf("Validation error", "Database error")
        val result = CommandResult.failure("Multiple errors occurred", errors)

        assertFalse(result.success)
        assertEquals("Multiple errors occurred", result.message)
        assertNull(result.data)
        assertEquals(errors, result.errors)
    }

    @Test
    fun `should support data class equality`() {
        val data = "test data"
        val errors = listOf("error1")
        
        val result1 = CommandResult(
            success = true,
            message = "Success",
            data = data,
            errors = errors
        )
        
        val result2 = CommandResult(
            success = true,
            message = "Success",
            data = data,
            errors = errors
        )

        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun `should support data class copy`() {
        val result = CommandResult(
            success = false,
            message = "Original message",
            data = "original data",
            errors = listOf("original error")
        )

        val copiedResult = result.copy(success = true, message = "Updated message")

        assertTrue(copiedResult.success)
        assertEquals("Updated message", copiedResult.message)
        assertEquals("original data", copiedResult.data)
        assertEquals(listOf("original error"), copiedResult.errors)
    }

    @Test
    fun `should handle empty errors list`() {
        val result = CommandResult(
            success = true,
            message = "Success",
            errors = emptyList()
        )

        assertTrue(result.errors.isEmpty())
        assertEquals(0, result.errors.size)
    }

    @Test
    fun `should handle null data`() {
        val result = CommandResult(
            success = true,
            message = "Success",
            data = null
        )

        assertNull(result.data)
    }

    @Test
    fun `should handle multiple errors`() {
        val errors = listOf("Error 1", "Error 2", "Error 3", "Error 4")
        val result = CommandResult.failure("Multiple validation errors", errors)

        assertFalse(result.success)
        assertEquals(4, result.errors.size)
        assertEquals(errors, result.errors)
    }

    @Test
    fun `should handle complex data objects`() {
        data class ComplexData(val id: Long, val name: String, val items: List<String>)
        val complexData = ComplexData(1L, "Test", listOf("item1", "item2"))
        
        val result = CommandResult.success("Complex data created", complexData)

        assertTrue(result.success)
        assertEquals(complexData, result.data)
    }

    @Test
    fun `should handle toString properly`() {
        val result = CommandResult.success("Test message", "test data")
        val toString = result.toString()

        assertTrue(toString.contains("success=true"))
        assertTrue(toString.contains("Test message"))
        assertTrue(toString.contains("test data"))
    }

    @Test
    fun `should handle different data types`() {
        val stringResult = CommandResult.success("String data", "test")
        val numberResult = CommandResult.success("Number data", 42)
        val listResult = CommandResult.success("List data", listOf(1, 2, 3))
        val mapResult = CommandResult.success("Map data", mapOf("key" to "value"))

        assertEquals("test", stringResult.data)
        assertEquals(42, numberResult.data)
        assertEquals(listOf(1, 2, 3), listResult.data)
        assertEquals(mapOf("key" to "value"), mapResult.data)
    }

    @Test
    fun `should handle success and failure scenarios`() {
        val successResult = CommandResult.success("All good")
        val failureResult = CommandResult.failure("Something went wrong")

        assertTrue(successResult.success)
        assertFalse(failureResult.success)
        assertEquals("All good", successResult.message)
        assertEquals("Something went wrong", failureResult.message)
    }

    @Test
    fun `should handle edge cases with empty strings`() {
        val result = CommandResult(
            success = true,
            message = "",
            data = "",
            errors = listOf("")
        )

        assertTrue(result.success)
        assertEquals("", result.message)
        assertEquals("", result.data)
        assertEquals(listOf(""), result.errors)
    }
} 