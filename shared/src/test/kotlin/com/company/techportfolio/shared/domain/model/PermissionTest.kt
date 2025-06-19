package com.company.techportfolio.shared.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class PermissionTest {

    @Test
    fun `should create permission with default values`() {
        val permission = Permission(
            name = "READ_PORTFOLIO",
            resource = "portfolio",
            action = "read"
        )

        assertEquals("READ_PORTFOLIO", permission.name)
        assertEquals("portfolio", permission.resource)
        assertEquals("read", permission.action)
        assertEquals(null, permission.id)
        assertEquals(null, permission.description)
        assertTrue(permission.isActive)
        assertNotNull(permission.createdAt)
    }

    @Test
    fun `should create permission with all parameters`() {
        val now = LocalDateTime.now()
        
        val permission = Permission(
            id = 1L,
            name = "WRITE_PORTFOLIO",
            description = "Permission to write portfolios",
            resource = "portfolio",
            action = "write",
            isActive = false,
            createdAt = now
        )

        assertEquals(1L, permission.id)
        assertEquals("WRITE_PORTFOLIO", permission.name)
        assertEquals("Permission to write portfolios", permission.description)
        assertEquals("portfolio", permission.resource)
        assertEquals("write", permission.action)
        assertFalse(permission.isActive)
        assertEquals(now, permission.createdAt)
    }

    @Test
    fun `getPermissionString should return resource and action concatenated`() {
        val permission = Permission(
            name = "READ_PORTFOLIO",
            resource = "portfolio",
            action = "read"
        )

        assertEquals("portfolio:read", permission.getPermissionString())
    }

    @Test
    fun `getPermissionString should handle empty resource and action`() {
        val permission = Permission(
            name = "EMPTY_PERMISSION",
            resource = "",
            action = ""
        )

        assertEquals(":", permission.getPermissionString())
    }

    @Test
    fun `getPermissionString should handle special characters`() {
        val permission = Permission(
            name = "SPECIAL_PERMISSION",
            resource = "user-profile",
            action = "create/update"
        )

        assertEquals("user-profile:create/update", permission.getPermissionString())
    }

    @Test
    fun `should support data class equality`() {
        val timestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        
        val permission1 = Permission(
            name = "READ_PORTFOLIO",
            resource = "portfolio",
            action = "read",
            createdAt = timestamp
        )

        val permission2 = Permission(
            name = "READ_PORTFOLIO",
            resource = "portfolio",
            action = "read",
            createdAt = timestamp
        )

        assertEquals(permission1, permission2)
        assertEquals(permission1.hashCode(), permission2.hashCode())
    }

    @Test
    fun `should support data class copy`() {
        val permission = Permission(
            name = "READ_PORTFOLIO",
            resource = "portfolio",
            action = "read"
        )

        val copiedPermission = permission.copy(action = "write")

        assertEquals("READ_PORTFOLIO", copiedPermission.name)
        assertEquals("portfolio", copiedPermission.resource)
        assertEquals("write", copiedPermission.action)
    }

    @Test
    fun `should handle different resource types`() {
        val permissions = listOf(
            Permission(name = "READ_USER", resource = "user", action = "read"),
            Permission(name = "READ_ROLE", resource = "role", action = "read"),
            Permission(name = "READ_TECHNOLOGY", resource = "technology", action = "read"),
            Permission(name = "READ_ASSESSMENT", resource = "assessment", action = "read")
        )

        assertEquals(4, permissions.size)
        permissions.forEach { permission ->
            assertNotNull(permission.name)
            assertNotNull(permission.resource)
            assertNotNull(permission.action)
            assertTrue(permission.isActive)
        }
    }

    @Test
    fun `should handle different action types`() {
        val actions = listOf("create", "read", "update", "delete", "manage", "execute")
        val permissions = actions.map { action ->
            Permission(name = "TEST_${action.uppercase()}", resource = "test", action = action)
        }

        assertEquals(6, permissions.size)
        permissions.forEachIndexed { index, permission ->
            assertEquals(actions[index], permission.action)
            assertTrue(permission.isActive)
        }
    }

    @Test
    fun `should handle null optional fields`() {
        val permission = Permission(
            name = "TEST_PERMISSION",
            resource = "test",
            action = "read",
            id = null,
            description = null
        )

        assertNull(permission.id)
        assertNull(permission.description)
        assertNotNull(permission.createdAt)
    }

    @Test
    fun `should handle toString properly`() {
        val permission = Permission(
            name = "READ_PORTFOLIO",
            resource = "portfolio",
            action = "read"
        )

        val toString = permission.toString()
        assertTrue(toString.contains("READ_PORTFOLIO"))
        assertTrue(toString.contains("portfolio"))
        assertTrue(toString.contains("read"))
    }

    @Test
    fun `should create different permissions with different permission strings`() {
        val permission1 = Permission(
            name = "READ_PORTFOLIO",
            resource = "portfolio",
            action = "read"
        )

        val permission2 = Permission(
            name = "WRITE_PORTFOLIO",
            resource = "portfolio",
            action = "write"
        )

        val permission3 = Permission(
            name = "READ_USER",
            resource = "user",
            action = "read"
        )

        assertEquals("portfolio:read", permission1.getPermissionString())
        assertEquals("portfolio:write", permission2.getPermissionString())
        assertEquals("user:read", permission3.getPermissionString())
    }
} 