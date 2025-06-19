package com.company.techportfolio.shared.domain.port

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

// Test concrete implementation of Command
data class TestCommand(
    val commandData: String,
    val customCommandId: String? = null,
    val customTimestamp: LocalDateTime? = null,
    val customVersion: String? = null
) : Command(
    customCommandId ?: java.util.UUID.randomUUID().toString(),
    customTimestamp ?: LocalDateTime.now(),
    customVersion ?: "1.0"
)

class CommandTest {

    @Test
    fun `should create command with default values`() {
        val command = TestCommand("test data")

        assertEquals("test data", command.commandData)
        assertNotNull(command.commandId)
        assertNotNull(command.timestamp)
        assertEquals("1.0", command.version)
    }

    @Test
    fun `should create command with custom values`() {
        val customCommandId = "custom-command-id"
        val customTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        val customVersion = "2.0"

        val command = TestCommand(
            commandData = "custom test data",
            customCommandId = customCommandId,
            customTimestamp = customTimestamp,
            customVersion = customVersion
        )

        assertEquals("custom test data", command.commandData)
        assertEquals(customCommandId, command.commandId)
        assertEquals(customTimestamp, command.timestamp)
        assertEquals(customVersion, command.version)
    }

    @Test
    fun `should generate unique command IDs`() {
        val command1 = TestCommand("data1")
        val command2 = TestCommand("data2")

        assertNotEquals(command1.commandId, command2.commandId)
    }

    @Test
    fun `should support data class equality for concrete commands`() {
        val commandId = "same-id"
        val timestamp = LocalDateTime.now()
        
        val command1 = TestCommand(
            commandData = "same data",
            customCommandId = commandId,
            customTimestamp = timestamp,
            customVersion = "1.0"
        )
        
        val command2 = TestCommand(
            commandData = "same data",
            customCommandId = commandId,
            customTimestamp = timestamp,
            customVersion = "1.0"
        )

        assertEquals(command1, command2)
        assertEquals(command1.hashCode(), command2.hashCode())
    }

    @Test
    fun `should support data class copy for concrete commands`() {
        val command = TestCommand(
            commandData = "original data",
            customCommandId = "test-id",
            customTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0),
            customVersion = "1.0"
        )
        val copiedCommand = command.copy(commandData = "modified data")

        assertEquals("modified data", copiedCommand.commandData)
        assertEquals("test-id", copiedCommand.customCommandId)
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0, 0), copiedCommand.customTimestamp)
        assertEquals("1.0", copiedCommand.customVersion)
    }
} 