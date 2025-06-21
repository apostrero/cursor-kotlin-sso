package com.company.techportfolio.shared.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class RoleTest {

    @Test
    fun `should create role with default values`() {
        val role = Role(
            name = "USER"
        )

        assertEquals("USER", role.name)
        assertEquals(null, role.id)
        assertEquals(null, role.description)
        assertTrue(role.isActive)
        assertTrue(role.permissions.isEmpty())
        assertNotNull(role.createdAt)
    }

    @Test
    fun `should create role with all parameters`() {
        val now = LocalDateTime.now()
        val permission1 = Permission(id = 1, name = "READ", resource = "portfolio", action = "read")
        val permission2 = Permission(id = 2, name = "WRITE", resource = "portfolio", action = "write")

        val role = Role(
            id = 1L,
            name = "ADMIN",
            description = "Administrator role",
            isActive = false,
            createdAt = now,
            permissions = setOf(permission1, permission2)
        )

        assertEquals(1L, role.id)
        assertEquals("ADMIN", role.name)
        assertEquals("Administrator role", role.description)
        assertFalse(role.isActive)
        assertEquals(now, role.createdAt)
        assertEquals(2, role.permissions.size)
        assertTrue(role.permissions.contains(permission1))
        assertTrue(role.permissions.contains(permission2))
    }

    @Test
    fun `should support data class equality`() {
        val timestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val role1 = Role(
            name = "USER",
            createdAt = timestamp
        )

        val role2 = Role(
            name = "USER",
            createdAt = timestamp
        )

        assertEquals(role1, role2)
        assertEquals(role1.hashCode(), role2.hashCode())
    }

    @Test
    fun `should support data class copy`() {
        val role = Role(
            name = "USER"
        )

        val copiedRole = role.copy(name = "ADMIN")

        assertEquals("ADMIN", copiedRole.name)
        assertTrue(copiedRole.isActive)
        assertTrue(copiedRole.permissions.isEmpty())
    }

    @Test
    fun `should handle role with single permission`() {
        val permission = Permission(id = 1, name = "READ", resource = "portfolio", action = "read")
        val role = Role(
            name = "READER",
            permissions = setOf(permission)
        )

        assertEquals(1, role.permissions.size)
        assertTrue(role.permissions.contains(permission))
    }

    @Test
    fun `should handle role with multiple permissions`() {
        val permission1 = Permission(id = 1, name = "READ", resource = "portfolio", action = "read")
        val permission2 = Permission(id = 2, name = "WRITE", resource = "portfolio", action = "write")
        val permission3 = Permission(id = 3, name = "DELETE", resource = "portfolio", action = "delete")

        val role = Role(
            name = "PORTFOLIO_MANAGER",
            permissions = setOf(permission1, permission2, permission3)
        )

        assertEquals(3, role.permissions.size)
        assertTrue(role.permissions.contains(permission1))
        assertTrue(role.permissions.contains(permission2))
        assertTrue(role.permissions.contains(permission3))
    }

    @Test
    fun `should handle role with empty permissions`() {
        val role = Role(
            name = "EMPTY_ROLE",
            permissions = emptySet()
        )

        assertTrue(role.permissions.isEmpty())
        assertEquals(0, role.permissions.size)
    }

    @Test
    fun `should handle different role types`() {
        val roles = listOf(
            Role(name = "USER"),
            Role(name = "ADMIN"),
            Role(name = "MANAGER"),
            Role(name = "GUEST"),
            Role(name = "SUPER_ADMIN")
        )

        assertEquals(5, roles.size)
        roles.forEach { role ->
            assertNotNull(role.name)
            assertTrue(role.isActive)
            assertTrue(role.permissions.isEmpty())
        }
    }

    @Test
    fun `should validate required fields are not null`() {
        val role = Role(
            name = "TEST_ROLE"
        )

        assertNotNull(role.name)
    }

    @Test
    fun `should handle null optional fields`() {
        val role = Role(
            name = "TEST_ROLE",
            id = null,
            description = null
        )

        assertNull(role.id)
        assertNull(role.description)
        assertNotNull(role.createdAt)
    }

    @Test
    fun `should handle toString properly`() {
        val role = Role(
            name = "ADMIN",
            description = "Administrator role"
        )

        val toString = role.toString()
        assertTrue(toString.contains("ADMIN"))
        assertTrue(toString.contains("Administrator role"))
    }

    @Test
    fun `should handle permissions with different resources`() {
        val portfolioRead = Permission(name = "PORTFOLIO_READ", resource = "portfolio", action = "read")
        val userWrite = Permission(name = "USER_WRITE", resource = "user", action = "write")
        val roleManage = Permission(name = "ROLE_MANAGE", resource = "role", action = "manage")

        val role = Role(
            name = "MULTI_RESOURCE_ROLE",
            permissions = setOf(portfolioRead, userWrite, roleManage)
        )

        assertEquals(3, role.permissions.size)
        val resources = role.permissions.map { it.resource }.toSet()
        assertEquals(setOf("portfolio", "user", "role"), resources)
    }

    @Test
    fun `should handle role hierarchy concept`() {
        val basicPermission = Permission(name = "READ", resource = "portfolio", action = "read")
        val adminPermission = Permission(name = "MANAGE", resource = "portfolio", action = "manage")

        val basicRole = Role(
            name = "BASIC_USER",
            permissions = setOf(basicPermission)
        )

        val adminRole = Role(
            name = "ADMIN",
            permissions = setOf(basicPermission, adminPermission)
        )

        assertEquals(1, basicRole.permissions.size)
        assertEquals(2, adminRole.permissions.size)
        assertTrue(adminRole.permissions.containsAll(basicRole.permissions))
    }
} 