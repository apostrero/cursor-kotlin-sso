package com.company.techportfolio.authorization.domain.service

import com.company.techportfolio.authorization.domain.model.AuthorizationRequest
import com.company.techportfolio.authorization.domain.model.AuthorizationResponse
import com.company.techportfolio.authorization.domain.model.UserPermissions
import com.company.techportfolio.authorization.domain.port.UserRepository
import com.company.techportfolio.authorization.domain.port.RoleRepository
import com.company.techportfolio.authorization.domain.port.PermissionRepository
import com.company.techportfolio.shared.domain.model.User
import com.company.techportfolio.shared.domain.model.Role
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*

class AuthorizationServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var roleRepository: RoleRepository
    private lateinit var permissionRepository: PermissionRepository
    private lateinit var authorizationService: AuthorizationService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        roleRepository = mockk()
        permissionRepository = mockk()
        authorizationService = AuthorizationService(userRepository, roleRepository, permissionRepository)
    }

    @Test
    fun `should authorize user when user is active and has permission`() {
        // Given
        val request = AuthorizationRequest(
            username = "testuser",
            resource = "portfolio",
            action = "read"
        )

        every { userRepository.isUserActive("testuser") } returns true
        every { permissionRepository.hasPermission("testuser", "portfolio", "read") } returns true
        every { userRepository.findUserPermissions("testuser") } returns listOf("portfolio:read", "portfolio:write")
        every { userRepository.findUserRoles("testuser") } returns listOf("USER", "PORTFOLIO_MANAGER")
        every { userRepository.findUserOrganization("testuser") } returns 1L

        // When
        val result = authorizationService.authorizeUser(request)

        // Then
        assertTrue(result.isAuthorized)
        assertEquals("testuser", result.username)
        assertEquals("portfolio", result.resource)
        assertEquals("read", result.action)
        assertEquals(listOf("portfolio:read", "portfolio:write"), result.permissions)
        assertEquals(listOf("USER", "PORTFOLIO_MANAGER"), result.roles)
        assertEquals(1L, result.organizationId)

        verify {
            userRepository.isUserActive("testuser")
            permissionRepository.hasPermission("testuser", "portfolio", "read")
            userRepository.findUserPermissions("testuser")
            userRepository.findUserRoles("testuser")
            userRepository.findUserOrganization("testuser")
        }
    }

    @Test
    fun `should not authorize user when user is not active`() {
        // Given
        val request = AuthorizationRequest(
            username = "inactiveuser",
            resource = "portfolio",
            action = "read"
        )

        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.authorizeUser(request)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals("inactiveuser", result.username)
        assertEquals("User is not active or does not exist", result.errorMessage)

        verify {
            userRepository.isUserActive("inactiveuser")
        }
        verify(exactly = 0) {
            permissionRepository.hasPermission(any(), any(), any())
        }
    }

    @Test
    fun `should not authorize user when user does not have permission`() {
        // Given
        val request = AuthorizationRequest(
            username = "testuser",
            resource = "admin",
            action = "delete"
        )

        every { userRepository.isUserActive("testuser") } returns true
        every { permissionRepository.hasPermission("testuser", "admin", "delete") } returns false

        // When
        val result = authorizationService.authorizeUser(request)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals("testuser", result.username)
        assertEquals("User does not have permission for admin:delete", result.errorMessage)

        verify {
            userRepository.isUserActive("testuser")
            permissionRepository.hasPermission("testuser", "admin", "delete")
        }
    }

    @Test
    fun `should handle exception during authorization`() {
        // Given
        val request = AuthorizationRequest(
            username = "testuser",
            resource = "portfolio",
            action = "read"
        )

        every { userRepository.isUserActive("testuser") } throws RuntimeException("Database error")

        // When
        val result = authorizationService.authorizeUser(request)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals("testuser", result.username)
        assertEquals("Authorization failed: Database error", result.errorMessage)
    }

    @Test
    fun `should return user permissions for active user`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findUserPermissions("testuser") } returns listOf("portfolio:read", "portfolio:write")
        every { userRepository.findUserRoles("testuser") } returns listOf("USER", "PORTFOLIO_MANAGER")
        every { userRepository.findUserOrganization("testuser") } returns 1L

        // When
        val result = authorizationService.getUserPermissions("testuser")

        // Then
        assertEquals("testuser", result.username)
        assertEquals(listOf("portfolio:read", "portfolio:write"), result.permissions)
        assertEquals(listOf("USER", "PORTFOLIO_MANAGER"), result.roles)
        assertEquals(1L, result.organizationId)
        assertTrue(result.isActive)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findUserPermissions("testuser")
            userRepository.findUserRoles("testuser")
            userRepository.findUserOrganization("testuser")
        }
    }

    @Test
    fun `should return empty permissions for inactive user`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.getUserPermissions("inactiveuser")

        // Then
        assertEquals("inactiveuser", result.username)
        assertTrue(result.permissions.isEmpty())
        assertTrue(result.roles.isEmpty())
        assertFalse(result.isActive)

        verify {
            userRepository.isUserActive("inactiveuser")
        }
        verify(exactly = 0) {
            userRepository.findUserPermissions(any())
            userRepository.findUserRoles(any())
            userRepository.findUserOrganization(any())
        }
    }

    @Test
    fun `should return true when user has specific role`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findUserRoles("testuser") } returns listOf("USER", "PORTFOLIO_MANAGER")

        // When
        val result = authorizationService.hasRole("testuser", "PORTFOLIO_MANAGER")

        // Then
        assertTrue(result)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findUserRoles("testuser")
        }
    }

    @Test
    fun `should return false when user does not have specific role`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findUserRoles("testuser") } returns listOf("USER")

        // When
        val result = authorizationService.hasRole("testuser", "PORTFOLIO_MANAGER")

        // Then
        assertFalse(result)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findUserRoles("testuser")
        }
    }

    @Test
    fun `should return false when user is not active for role check`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.hasRole("inactiveuser", "ADMIN")

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return true when user has any of the specified roles`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findUserRoles("testuser") } returns listOf("USER", "PORTFOLIO_MANAGER")

        // When
        val result = authorizationService.hasAnyRole("testuser", listOf("ADMIN", "PORTFOLIO_MANAGER"))

        // Then
        assertTrue(result)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findUserRoles("testuser")
        }
    }

    @Test
    fun `should return false when user does not have any of the specified roles`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findUserRoles("testuser") } returns listOf("USER")

        // When
        val result = authorizationService.hasAnyRole("testuser", listOf("ADMIN", "PORTFOLIO_MANAGER"))

        // Then
        assertFalse(result)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findUserRoles("testuser")
        }
    }

    @Test
    fun `should return false when user is not active for any role check`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.hasAnyRole("inactiveuser", listOf("ADMIN", "PORTFOLIO_MANAGER"))

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return true when user has specific permission`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { permissionRepository.hasPermission("testuser", "portfolio", "read") } returns true

        // When
        val result = authorizationService.hasPermission("testuser", "portfolio", "read")

        // Then
        assertTrue(result)

        verify {
            userRepository.isUserActive("testuser")
            permissionRepository.hasPermission("testuser", "portfolio", "read")
        }
    }

    @Test
    fun `should return false when user does not have specific permission`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { permissionRepository.hasPermission("testuser", "portfolio", "write") } returns false

        // When
        val result = authorizationService.hasPermission("testuser", "portfolio", "write")

        // Then
        assertFalse(result)

        verify {
            userRepository.isUserActive("testuser")
            permissionRepository.hasPermission("testuser", "portfolio", "write")
        }
    }

    @Test
    fun `should return false when user is not active for permission check`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.hasPermission("inactiveuser", "portfolio", "read")

        // Then
        assertFalse(result)

        verify {
            userRepository.isUserActive("inactiveuser")
        }
        verify(exactly = 0) {
            permissionRepository.hasPermission(any(), any(), any())
        }
    }

    @Test
    fun `should return true when user has any of the specified permissions`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { permissionRepository.hasPermission("testuser", "portfolio", "read") } returns true
        every { permissionRepository.hasPermission("testuser", "portfolio", "write") } returns false

        // When
        val result = authorizationService.hasAnyPermission("testuser", "portfolio", listOf("read", "write"))

        // Then
        assertTrue(result)

        verify {
            userRepository.isUserActive("testuser")
            permissionRepository.hasPermission("testuser", "portfolio", "read")
        }
        // Note: write permission is not checked due to short-circuit evaluation
    }

    @Test
    fun `should return false when user does not have any of the specified permissions`() {
        // Given
        every { userRepository.isUserActive("testuser") } returns true
        every { permissionRepository.hasPermission("testuser", "portfolio", "read") } returns false
        every { permissionRepository.hasPermission("testuser", "portfolio", "write") } returns false

        // When
        val result = authorizationService.hasAnyPermission("testuser", "portfolio", listOf("read", "write"))

        // Then
        assertFalse(result)

        verify {
            userRepository.isUserActive("testuser")
            permissionRepository.hasPermission("testuser", "portfolio", "read")
            permissionRepository.hasPermission("testuser", "portfolio", "write")
        }
    }

    @Test
    fun `should return false when user is not active for any permission check`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.hasAnyPermission("inactiveuser", "portfolio", listOf("read", "write"))

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return user details for active user`() {
        // Given
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            isActive = true,
            organizationId = 1L,
            roles = setOf(Role(1L, "USER"), Role(2L, "PORTFOLIO_MANAGER"))
        )

        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findById("testuser") } returns user
        every { userRepository.findUserRoles("testuser") } returns listOf("USER", "PORTFOLIO_MANAGER")

        // When
        val result = authorizationService.getUserDetails("testuser")

        // Then
        assertTrue(result.isAuthorized)
        assertEquals("testuser", result.username)
        assertEquals(listOf("USER", "PORTFOLIO_MANAGER"), result.roles)
        assertEquals(1L, result.organizationId)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findById("testuser")
            userRepository.findUserRoles("testuser")
        }
    }

    @Test
    fun `should return unauthorized for inactive user details`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.getUserDetails("inactiveuser")

        // Then
        assertFalse(result.isAuthorized)
        assertEquals("inactiveuser", result.username)
        assertEquals("User is not active or does not exist", result.errorMessage)

        verify {
            userRepository.isUserActive("inactiveuser")
        }
        verify(exactly = 0) {
            userRepository.findById(any())
        }
    }

    @Test
    fun `should return unauthorized for non-existent user details`() {
        // Given
        every { userRepository.isUserActive("nonexistent") } returns true
        every { userRepository.findById("nonexistent") } returns null

        // When
        val result = authorizationService.getUserDetails("nonexistent")

        // Then
        assertFalse(result.isAuthorized)
        assertEquals("nonexistent", result.username)
        assertEquals("User not found", result.errorMessage)

        verify {
            userRepository.isUserActive("nonexistent")
            userRepository.findById("nonexistent")
        }
    }

    @Test
    fun `should return user details with empty roles when user has no roles`() {
        // Given
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            isActive = true,
            organizationId = 1L,
            roles = emptySet()
        )

        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findById("testuser") } returns user
        every { userRepository.findUserRoles("testuser") } returns emptyList()

        // When
        val result = authorizationService.getUserDetails("testuser")

        // Then
        assertTrue(result.isAuthorized)
        assertEquals("testuser", result.username)
        assertTrue(result.roles.isEmpty())
        assertEquals(1L, result.organizationId)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findById("testuser")
            userRepository.findUserRoles("testuser")
        }
    }

    @Test
    fun `should return user details with organization id when user has organization`() {
        // Given
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            isActive = true,
            organizationId = 100L,
            roles = setOf(Role(1L, "USER"))
        )

        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findById("testuser") } returns user
        every { userRepository.findUserRoles("testuser") } returns listOf("USER")

        // When
        val result = authorizationService.getUserDetails("testuser")

        // Then
        assertTrue(result.isAuthorized)
        assertEquals("testuser", result.username)
        assertEquals(listOf("USER"), result.roles)
        assertEquals(100L, result.organizationId)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findById("testuser")
            userRepository.findUserRoles("testuser")
        }
    }

    @Test
    fun `should return user details with null organization id when user has no organization`() {
        // Given
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            isActive = true,
            organizationId = null,
            roles = setOf(Role(1L, "USER"))
        )

        every { userRepository.isUserActive("testuser") } returns true
        every { userRepository.findById("testuser") } returns user
        every { userRepository.findUserRoles("testuser") } returns listOf("USER")

        // When
        val result = authorizationService.getUserDetails("testuser")

        // Then
        assertTrue(result.isAuthorized)
        assertEquals("testuser", result.username)
        assertEquals(listOf("USER"), result.roles)
        assertNull(result.organizationId)

        verify {
            userRepository.isUserActive("testuser")
            userRepository.findById("testuser")
            userRepository.findUserRoles("testuser")
        }
    }

    @Test
    fun `should handle exception during user details retrieval`() {
        // Given
        every { userRepository.isUserActive("testuser") } throws RuntimeException("Database error")

        // When
        val result = authorizationService.getUserDetails("testuser")

        // Then
        assertFalse(result.isAuthorized)
        assertEquals("testuser", result.username)
        assertEquals("Authorization failed: Database error", result.errorMessage)

        verify {
            userRepository.isUserActive("testuser")
        }
    }
} 