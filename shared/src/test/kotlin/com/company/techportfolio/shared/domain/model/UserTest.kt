package com.company.techportfolio.shared.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class UserTest {

    @Test
    fun `should create user with default values`() {
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertTrue(user.isActive)
        assertTrue(user.isEnabled)
        assertEquals(null, user.id)
        assertEquals(null, user.updatedAt)
        assertEquals(null, user.lastLoginAt)
        assertEquals(null, user.organizationId)
        assertTrue(user.roles.isEmpty())
        assertNotNull(user.createdAt)
    }

    @Test
    fun `should create user with all parameters`() {
        val now = LocalDateTime.now()
        val role1 = Role(id = 1, name = "USER")
        val role2 = Role(id = 2, name = "ADMIN")
        
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            isActive = false,
            isEnabled = false,
            createdAt = now,
            updatedAt = now,
            lastLoginAt = now,
            organizationId = 100L,
            roles = setOf(role1, role2)
        )

        assertEquals(1L, user.id)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertFalse(user.isActive)
        assertFalse(user.isEnabled)
        assertEquals(now, user.createdAt)
        assertEquals(now, user.updatedAt)
        assertEquals(now, user.lastLoginAt)
        assertEquals(100L, user.organizationId)
        assertEquals(2, user.roles.size)
        assertTrue(user.roles.contains(role1))
        assertTrue(user.roles.contains(role2))
    }

    @Test
    fun `getFullName should return first and last name concatenated`() {
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        assertEquals("John Doe", user.getFullName())
    }

    @Test
    fun `hasRole should return true if user has the specified role`() {
        val role = Role(name = "ADMIN")
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            roles = setOf(role)
        )

        assertTrue(user.hasRole("ADMIN"))
        assertFalse(user.hasRole("USER"))
    }

    @Test
    fun `hasAnyRole should return true if user has any of the specified roles`() {
        val adminRole = Role(name = "ADMIN")
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            roles = setOf(adminRole)
        )

        assertTrue(user.hasAnyRole(listOf("ADMIN", "MANAGER")))
        assertTrue(user.hasAnyRole(listOf("USER", "ADMIN")))
        assertFalse(user.hasAnyRole(listOf("USER", "MANAGER")))
    }

    @Test
    fun `isActiveAndEnabled should return true only if both active and enabled`() {
        val activeEnabledUser = User(
            username = "testuser1",
            email = "test1@example.com",
            firstName = "John",
            lastName = "Doe",
            isActive = true,
            isEnabled = true
        )

        val activeDisabledUser = User(
            username = "testuser2",
            email = "test2@example.com",
            firstName = "Jane",
            lastName = "Doe",
            isActive = true,
            isEnabled = false
        )

        val inactiveEnabledUser = User(
            username = "testuser3",
            email = "test3@example.com",
            firstName = "Bob",
            lastName = "Doe",
            isActive = false,
            isEnabled = true
        )

        val inactiveDisabledUser = User(
            username = "testuser4",
            email = "test4@example.com",
            firstName = "Alice",
            lastName = "Doe",
            isActive = false,
            isEnabled = false
        )

        assertTrue(activeEnabledUser.isActiveAndEnabled())
        assertFalse(activeDisabledUser.isActiveAndEnabled())
        assertFalse(inactiveEnabledUser.isActiveAndEnabled())
        assertFalse(inactiveDisabledUser.isActiveAndEnabled())
    }

    @Test
    fun `should support data class equality`() {
        val timestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        
        val user1 = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            createdAt = timestamp
        )

        val user2 = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            createdAt = timestamp
        )

        assertEquals(user1, user2)
        assertEquals(user1.hashCode(), user2.hashCode())
    }

    @Test
    fun `should support data class copy`() {
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        val copiedUser = user.copy(firstName = "Jane")

        assertEquals("Jane", copiedUser.firstName)
        assertEquals("testuser", copiedUser.username)
        assertEquals("test@example.com", copiedUser.email)
        assertEquals("Doe", copiedUser.lastName)
    }

    @Test
    fun `should handle user with single role`() {
        val role = Role(id = 1, name = "USER")
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            roles = setOf(role)
        )

        assertEquals(1, user.roles.size)
        assertTrue(user.roles.contains(role))
    }

    @Test
    fun `should handle user with multiple roles`() {
        val role1 = Role(id = 1, name = "USER")
        val role2 = Role(id = 2, name = "ADMIN")
        val role3 = Role(id = 3, name = "MANAGER")
        
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            roles = setOf(role1, role2, role3)
        )

        assertEquals(3, user.roles.size)
        assertTrue(user.roles.contains(role1))
        assertTrue(user.roles.contains(role2))
        assertTrue(user.roles.contains(role3))
    }

    @Test
    fun `should handle user with empty roles`() {
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            roles = emptySet()
        )

        assertTrue(user.roles.isEmpty())
        assertEquals(0, user.roles.size)
    }

    @Test
    fun `should validate required fields are not null`() {
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        assertNotNull(user.username)
        assertNotNull(user.email)
        assertNotNull(user.firstName)
        assertNotNull(user.lastName)
    }

    @Test
    fun `should handle null optional fields`() {
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            id = null,
            updatedAt = null,
            lastLoginAt = null,
            organizationId = null
        )

        assertNull(user.id)
        assertNull(user.updatedAt)
        assertNull(user.lastLoginAt)
        assertNull(user.organizationId)
        assertNotNull(user.createdAt)
    }

    @Test
    fun `should handle toString properly`() {
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        val toString = user.toString()
        assertTrue(toString.contains("testuser"))
        assertTrue(toString.contains("test@example.com"))
        assertTrue(toString.contains("John"))
        assertTrue(toString.contains("Doe"))
    }

    @Test
    fun `should handle user with complex role hierarchy`() {
        val readPermission = Permission(name = "READ", resource = "portfolio", action = "read")
        val writePermission = Permission(name = "WRITE", resource = "portfolio", action = "write")
        val adminPermission = Permission(name = "ADMIN", resource = "system", action = "manage")
        
        val userRole = Role(name = "USER", permissions = setOf(readPermission))
        val managerRole = Role(name = "MANAGER", permissions = setOf(readPermission, writePermission))
        val adminRole = Role(name = "ADMIN", permissions = setOf(readPermission, writePermission, adminPermission))
        
        val user = User(
            username = "superuser",
            email = "super@example.com",
            firstName = "Super",
            lastName = "User",
            roles = setOf(userRole, managerRole, adminRole)
        )

        assertEquals(3, user.roles.size)
        val allPermissions = user.roles.flatMap { it.permissions }.toSet()
        assertEquals(3, allPermissions.size)
        assertTrue(allPermissions.contains(readPermission))
        assertTrue(allPermissions.contains(writePermission))
        assertTrue(allPermissions.contains(adminPermission))
    }

    @Test
    fun `should handle email validation format`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "123@numbers.com"
        )
        
        val users = validEmails.mapIndexed { index, email ->
            User(
                username = "user$index",
                email = email,
                firstName = "John",
                lastName = "Doe"
            )
        }

        assertEquals(4, users.size)
        users.forEachIndexed { index, user ->
            assertEquals(validEmails[index], user.email)
            assertTrue(user.email.contains("@"))
        }
    }

    @Test
    fun `should handle different organization assignments`() {
        val users = listOf(
            User(username = "user1", email = "user1@example.com", firstName = "John", lastName = "Doe", organizationId = 1L),
            User(username = "user2", email = "user2@example.com", firstName = "Jane", lastName = "Doe", organizationId = 2L),
            User(username = "user3", email = "user3@example.com", firstName = "Bob", lastName = "Doe", organizationId = null)
        )

        assertEquals(1L, users[0].organizationId)
        assertEquals(2L, users[1].organizationId)
        assertNull(users[2].organizationId)
    }
} 