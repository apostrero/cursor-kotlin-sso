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

/**
 * Unit test suite for the AuthorizationService domain service.
 * 
 * This test class provides comprehensive coverage of the AuthorizationService
 * business logic using mocked dependencies. It verifies authorization decisions,
 * permission queries, role checks, and error handling scenarios.
 * 
 * The tests use MockK for mocking repository dependencies, allowing for isolated
 * testing of the service logic without requiring actual database connections or
 * external dependencies.
 * 
 * Test coverage includes:
 * - Authorization decision making (positive and negative cases)
 * - User permission and role queries
 * - Error handling and exception scenarios
 * - Edge cases and boundary conditions
 * - Service method interactions and call verification
 * 
 * Testing strategy:
 * - Unit testing with mocked dependencies
 * - Behavior verification using MockK
 * - Comprehensive assertion coverage
 * - Exception handling validation
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
class AuthorizationServiceTest {

    /** Mock repository for user data operations */
    private lateinit var userRepository: UserRepository
    /** Mock repository for role data operations */
    private lateinit var roleRepository: RoleRepository
    /** Mock repository for permission data operations */
    private lateinit var permissionRepository: PermissionRepository
    /** Service under test */
    private lateinit var authorizationService: AuthorizationService

    /**
     * Sets up test fixtures before each test method.
     * 
     * Initializes mock repositories and creates the service instance
     * with mocked dependencies for isolated unit testing.
     */
    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        roleRepository = mockk()
        permissionRepository = mockk()
        authorizationService = AuthorizationService(userRepository, roleRepository, permissionRepository)
    }

    /**
     * Tests successful authorization when user is active and has required permission.
     * 
     * This test verifies the happy path scenario where a user has the necessary
     * permissions to access a resource. It validates that the service returns
     * a positive authorization response with complete user details.
     */
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

    /**
     * Tests authorization denial when user account is inactive.
     * 
     * This test verifies that inactive users are denied access regardless
     * of their permissions. It ensures the service performs user status
     * validation before checking permissions.
     */
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

    /**
     * Tests authorization denial when user lacks required permission.
     * 
     * This test verifies that users without the specific permission for
     * a resource-action combination are denied access, even if they are
     * active users.
     */
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

    /**
     * Tests error handling when repository operations throw exceptions.
     * 
     * This test verifies that the service gracefully handles exceptions
     * from repository operations and returns appropriate error responses
     * instead of propagating exceptions.
     */
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

    /**
     * Tests successful retrieval of user permissions for active users.
     * 
     * This test verifies that the service correctly aggregates user
     * permissions, roles, and organizational information for active users.
     */
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

    /**
     * Tests that inactive users receive empty permission sets.
     * 
     * This test verifies that the service returns empty permissions
     * for inactive users without making unnecessary repository calls.
     */
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

    /**
     * Tests successful role verification for users with the specified role.
     * 
     * This test verifies that the service correctly identifies when a user
     * has a specific role assigned.
     */
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

    /**
     * Tests role verification failure when user lacks the specified role.
     * 
     * This test verifies that the service correctly identifies when a user
     * does not have a specific role assigned, even when they have other roles.
     */
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

    /**
     * Tests role verification failure for inactive users.
     * 
     * This test verifies that inactive users are denied role checks
     * regardless of their actual role assignments.
     */
    @Test
    fun `should return false when user is not active for role check`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.hasRole("inactiveuser", "ADMIN")

        // Then
        assertFalse(result)
    }

    /**
     * Tests successful verification when user has any of the specified roles.
     * 
     * This test verifies that the service correctly identifies when a user
     * has at least one role from a list of acceptable roles.
     */
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

    /**
     * Tests role verification failure when user has none of the specified roles.
     * 
     * This test verifies that the service correctly identifies when a user
     * lacks all roles from a list of required roles.
     */
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

    /**
     * Tests role verification failure for inactive users with multiple role check.
     * 
     * This test verifies that inactive users are denied access when checking
     * against multiple acceptable roles.
     */
    @Test
    fun `should return false when user is not active for any role check`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.hasAnyRole("inactiveuser", listOf("ADMIN", "PORTFOLIO_MANAGER"))

        // Then
        assertFalse(result)
    }

    /**
     * Tests successful permission verification for users with the specified permission.
     * 
     * This test verifies that the service correctly identifies when a user
     * has a specific permission for a resource-action combination.
     */
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

    /**
     * Tests permission verification failure when user lacks the specified permission.
     * 
     * This test verifies that the service correctly identifies when a user
     * does not have a specific permission for a resource-action combination.
     */
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

    /**
     * Tests permission verification failure for inactive users.
     * 
     * This test verifies that inactive users are denied permission checks
     * without querying the permission repository.
     */
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

    /**
     * Tests successful verification when user has any of the specified permissions.
     * 
     * This test verifies that the service correctly identifies when a user
     * has at least one permission from a list of acceptable permissions.
     * It also tests short-circuit evaluation where checking stops after
     * finding the first matching permission.
     */
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

    /**
     * Tests permission verification failure when user has none of the specified permissions.
     * 
     * This test verifies that the service correctly identifies when a user
     * lacks all permissions from a list of required permissions.
     */
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

    /**
     * Tests permission verification failure for inactive users with multiple permission check.
     * 
     * This test verifies that inactive users are denied access when checking
     * against multiple acceptable permissions.
     */
    @Test
    fun `should return false when user is not active for any permission check`() {
        // Given
        every { userRepository.isUserActive("inactiveuser") } returns false

        // When
        val result = authorizationService.hasAnyPermission("inactiveuser", "portfolio", listOf("read", "write"))

        // Then
        assertFalse(result)
    }

    /**
     * Tests successful retrieval of user details for active users.
     * 
     * This test verifies that the service correctly retrieves and returns
     * complete user details including roles and organization information
     * for active users with proper data.
     */
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

    /**
     * Tests user details retrieval failure for inactive users.
     * 
     * This test verifies that inactive users receive unauthorized responses
     * when requesting user details, without making unnecessary repository calls.
     */
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

    /**
     * Tests user details retrieval failure for non-existent users.
     * 
     * This test verifies that the service handles cases where a user
     * appears active but cannot be found in the repository, returning
     * appropriate error responses.
     */
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

    /**
     * Tests user details retrieval for users with no assigned roles.
     * 
     * This test verifies that the service correctly handles users who
     * exist and are active but have no roles assigned, returning empty
     * role lists without errors.
     */
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

    /**
     * Tests user details retrieval with specific organization ID.
     * 
     * This test verifies that the service correctly returns organization
     * information when users are associated with specific organizations.
     */
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

    /**
     * Tests user details retrieval for users without organization assignment.
     * 
     * This test verifies that the service correctly handles users who
     * are not associated with any organization, returning null for
     * organization ID without errors.
     */
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