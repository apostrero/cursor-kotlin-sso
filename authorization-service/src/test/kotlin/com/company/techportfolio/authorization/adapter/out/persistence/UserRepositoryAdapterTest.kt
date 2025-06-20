package com.company.techportfolio.authorization.adapter.out.persistence

import com.company.techportfolio.authorization.adapter.out.persistence.entity.UserEntity
import com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity
import com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity
import com.company.techportfolio.authorization.adapter.out.persistence.repository.UserJpaRepository
import com.company.techportfolio.authorization.adapter.out.persistence.repository.RoleJpaRepository
import com.company.techportfolio.authorization.adapter.out.persistence.repository.PermissionJpaRepository
import com.company.techportfolio.shared.domain.model.User
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.util.*

/**
 * User Repository Adapter Test Suite - Persistence Layer Unit Tests
 *
 * This test class provides comprehensive unit testing for the UserRepositoryAdapter,
 * focusing on the adapter's interaction with JPA repositories and proper entity-to-domain
 * model mapping. The tests use MockK for mocking JPA repository dependencies.
 *
 * ## Test Strategy:
 * - **Unit Testing**: Isolated testing of adapter logic without database
 * - **Mock Dependencies**: JPA repositories mocked with MockK
 * - **Mapping Validation**: Entity-to-domain and domain-to-entity conversions
 * - **Relationship Testing**: User-role-permission relationship resolution
 * - **Edge Case Coverage**: Null handling, empty results, and error scenarios
 *
 * ## Test Coverage:
 * - User lookup operations (by ID, by username)
 * - User-role relationship queries
 * - User-permission resolution through roles
 * - Active/inactive user handling
 * - Null and empty result scenarios
 * - Entity-domain mapping accuracy
 *
 * ## Architecture:
 * - **Adapter**: UserRepositoryAdapter (system under test)
 * - **Dependencies**: UserJpaRepository, RoleJpaRepository, PermissionJpaRepository (mocked)
 * - **Framework**: JUnit 5 with MockK
 * - **Pattern**: Hexagonal architecture adapter testing
 *
 * ## Test Data:
 * Helper methods create consistent test entities and domain objects for reliable testing.
 *
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
class UserRepositoryAdapterTest {

    private val userJpaRepository = mockk<UserJpaRepository>()
    private val roleJpaRepository = mockk<RoleJpaRepository>()
    private val permissionJpaRepository = mockk<PermissionJpaRepository>()
    
    private lateinit var userRepositoryAdapter: UserRepositoryAdapter

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        userRepositoryAdapter = UserRepositoryAdapter(
            userJpaRepository,
            roleJpaRepository,
            permissionJpaRepository
        )
    }

    @Test
    fun `should find user by id successfully`() {
        // Given
        val username = "testuser"
        val userEntity = createUserEntity(username = username)
        
        every { userJpaRepository.findByUsername(username) } returns userEntity

        // When
        val result = userRepositoryAdapter.findById(username)

        // Then
        assertNotNull(result)
        assertEquals(username, result?.username)
        assertEquals("test@example.com", result?.email)
        assertEquals("Test", result?.firstName)
        assertEquals("User", result?.lastName)
        assertTrue(result?.isActive ?: false)
        assertEquals(1L, result?.organizationId)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should return null when user not found by id`() {
        // Given
        val username = "nonexistent"
        
        every { userJpaRepository.findByUsername(username) } returns null

        // When
        val result = userRepositoryAdapter.findById(username)

        // Then
        assertNull(result)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should find user by username successfully`() {
        // Given
        val username = "testuser"
        val userEntity = createUserEntity(username = username)
        
        every { userJpaRepository.findByUsername(username) } returns userEntity

        // When
        val result = userRepositoryAdapter.findByUsername(username)

        // Then
        assertNotNull(result)
        assertEquals(username, result?.username)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should return null when user not found by username`() {
        // Given
        val username = "nonexistent"
        
        every { userJpaRepository.findByUsername(username) } returns null

        // When
        val result = userRepositoryAdapter.findByUsername(username)

        // Then
        assertNull(result)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should find user with roles and permissions successfully`() {
        // Given
        val username = "testuser"
        val userEntity = createUserEntityWithRoles(username = username)
        
        every { userJpaRepository.findByUsername(username) } returns userEntity

        // When
        val result = userRepositoryAdapter.findUserWithRolesAndPermissions(username)

        // Then
        assertNotNull(result)
        assertEquals(username, result?.username)
        assertEquals(2, result?.roles?.size)
        assertTrue(result?.roles?.any { it.name == "USER" } ?: false)
        assertTrue(result?.roles?.any { it.name == "ADMIN" } ?: false)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should find user permissions successfully`() {
        // Given
        val username = "testuser"
        val permissions = listOf(
            createPermissionEntity(name = "portfolio:read", resource = "portfolio", action = "read"),
            createPermissionEntity(name = "portfolio:write", resource = "portfolio", action = "write")
        )
        
        every { permissionJpaRepository.findPermissionsByUsername(username) } returns permissions

        // When
        val result = userRepositoryAdapter.findUserPermissions(username)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains("portfolio:read"))
        assertTrue(result.contains("portfolio:write"))

        verify { permissionJpaRepository.findPermissionsByUsername(username) }
    }

    @Test
    fun `should return empty list when user has no permissions`() {
        // Given
        val username = "testuser"
        
        every { permissionJpaRepository.findPermissionsByUsername(username) } returns emptyList()

        // When
        val result = userRepositoryAdapter.findUserPermissions(username)

        // Then
        assertTrue(result.isEmpty())

        verify { permissionJpaRepository.findPermissionsByUsername(username) }
    }

    @Test
    fun `should find user roles successfully`() {
        // Given
        val username = "testuser"
        val roles = listOf(
            createRoleEntity(name = "USER"),
            createRoleEntity(name = "ADMIN")
        )
        
        every { roleJpaRepository.findRolesByUsername(username) } returns roles

        // When
        val result = userRepositoryAdapter.findUserRoles(username)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains("USER"))
        assertTrue(result.contains("ADMIN"))

        verify { roleJpaRepository.findRolesByUsername(username) }
    }

    @Test
    fun `should return empty list when user has no roles`() {
        // Given
        val username = "testuser"
        
        every { roleJpaRepository.findRolesByUsername(username) } returns emptyList()

        // When
        val result = userRepositoryAdapter.findUserRoles(username)

        // Then
        assertTrue(result.isEmpty())

        verify { roleJpaRepository.findRolesByUsername(username) }
    }

    @Test
    fun `should check if user is active successfully`() {
        // Given
        val username = "testuser"
        
        every { userJpaRepository.isUserActive(username) } returns true

        // When
        val result = userRepositoryAdapter.isUserActive(username)

        // Then
        assertTrue(result)

        verify { userJpaRepository.isUserActive(username) }
    }

    @Test
    fun `should return false when user is not active`() {
        // Given
        val username = "inactiveuser"
        
        every { userJpaRepository.isUserActive(username) } returns false

        // When
        val result = userRepositoryAdapter.isUserActive(username)

        // Then
        assertFalse(result)

        verify { userJpaRepository.isUserActive(username) }
    }

    @Test
    fun `should return false when user active status is null`() {
        // Given
        val username = "testuser"
        
        every { userJpaRepository.isUserActive(username) } returns null

        // When
        val result = userRepositoryAdapter.isUserActive(username)

        // Then
        assertFalse(result)

        verify { userJpaRepository.isUserActive(username) }
    }

    @Test
    fun `should find user organization successfully`() {
        // Given
        val username = "testuser"
        val organizationId = 1L
        
        every { userJpaRepository.findOrganizationIdByUsername(username) } returns organizationId

        // When
        val result = userRepositoryAdapter.findUserOrganization(username)

        // Then
        assertEquals(organizationId, result)

        verify { userJpaRepository.findOrganizationIdByUsername(username) }
    }

    @Test
    fun `should return null when user has no organization`() {
        // Given
        val username = "testuser"
        
        every { userJpaRepository.findOrganizationIdByUsername(username) } returns null

        // When
        val result = userRepositoryAdapter.findUserOrganization(username)

        // Then
        assertNull(result)

        verify { userJpaRepository.findOrganizationIdByUsername(username) }
    }

    @Test
    fun `should handle empty username`() {
        // Given
        val username = ""
        
        every { userJpaRepository.findByUsername(username) } returns null

        // When
        val result = userRepositoryAdapter.findByUsername(username)

        // Then
        assertNull(result)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should handle special characters in username`() {
        // Given
        val username = "test.user@example.com"
        val userEntity = createUserEntity(username = username)
        
        every { userJpaRepository.findByUsername(username) } returns userEntity

        // When
        val result = userRepositoryAdapter.findByUsername(username)

        // Then
        assertNotNull(result)
        assertEquals(username, result?.username)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should handle Unicode characters in username`() {
        // Given
        val username = "tëstüser"
        val userEntity = createUserEntity(username = username)
        
        every { userJpaRepository.findByUsername(username) } returns userEntity

        // When
        val result = userRepositoryAdapter.findByUsername(username)

        // Then
        assertNotNull(result)
        assertEquals(username, result?.username)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should map user entity with null optional fields correctly`() {
        // Given
        val username = "testuser"
        val userEntity = UserEntity(
            id = 1L,
            username = username,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            isActive = true,
            isEnabled = true,
            createdAt = LocalDateTime.now(),
            updatedAt = null,
            lastLoginAt = null,
            organizationId = null,
            roles = emptySet()
        )
        
        every { userJpaRepository.findByUsername(username) } returns userEntity

        // When
        val result = userRepositoryAdapter.findByUsername(username)

        // Then
        assertNotNull(result)
        assertEquals(username, result?.username)
        assertNull(result?.updatedAt)
        assertNull(result?.lastLoginAt)
        assertNull(result?.organizationId)
        assertTrue(result?.roles?.isEmpty() ?: false)

        verify { userJpaRepository.findByUsername(username) }
    }

    @Test
    fun `should map user entity with all fields correctly`() {
        // Given
        val username = "testuser"
        val now = LocalDateTime.now()
        val userEntity = UserEntity(
            id = 1L,
            username = username,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            isActive = true,
            isEnabled = false,
            createdAt = now,
            updatedAt = now.plusHours(1),
            lastLoginAt = now.plusHours(2),
            organizationId = 5L,
            roles = setOf(createRoleEntity(name = "USER"))
        )
        
        every { userJpaRepository.findByUsername(username) } returns userEntity

        // When
        val result = userRepositoryAdapter.findByUsername(username)

        // Then
        assertNotNull(result)
        assertEquals(username, result?.username)
        assertEquals("test@example.com", result?.email)
        assertEquals("Test", result?.firstName)
        assertEquals("User", result?.lastName)
        assertTrue(result?.isActive ?: false)
        assertFalse(result?.isEnabled ?: true)
        assertEquals(now, result?.createdAt)
        assertEquals(now.plusHours(1), result?.updatedAt)
        assertEquals(now.plusHours(2), result?.lastLoginAt)
        assertEquals(5L, result?.organizationId)
        assertEquals(1, result?.roles?.size)

        verify { userJpaRepository.findByUsername(username) }
    }

    private fun createUserEntity(
        id: Long = 1L,
        username: String = "testuser",
        email: String = "test@example.com",
        firstName: String = "Test",
        lastName: String = "User",
        isActive: Boolean = true,
        organizationId: Long? = 1L
    ): UserEntity {
        return UserEntity(
            id = id,
            username = username,
            email = email,
            firstName = firstName,
            lastName = lastName,
            isActive = isActive,
            isEnabled = true,
            createdAt = LocalDateTime.now(),
            organizationId = organizationId,
            roles = emptySet()
        )
    }

    private fun createUserEntityWithRoles(
        id: Long = 1L,
        username: String = "testuser"
    ): UserEntity {
        val roles = setOf(
            createRoleEntity(name = "USER"),
            createRoleEntity(name = "ADMIN")
        )
        
        return UserEntity(
            id = id,
            username = username,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            isActive = true,
            isEnabled = true,
            createdAt = LocalDateTime.now(),
            organizationId = 1L,
            roles = roles
        )
    }

    private fun createRoleEntity(
        id: Long = 1L,
        name: String = "USER",
        description: String? = "User role",
        isActive: Boolean = true
    ): RoleEntity {
        return RoleEntity(
            id = id,
            name = name,
            description = description,
            isActive = isActive,
            createdAt = LocalDateTime.now(),
            permissions = emptySet()
        )
    }

    private fun createPermissionEntity(
        id: Long = 1L,
        name: String = "portfolio:read",
        description: String? = "Read portfolio permission",
        resource: String = "portfolio",
        action: String = "read",
        isActive: Boolean = true
    ): PermissionEntity {
        return PermissionEntity(
            id = id,
            name = name,
            description = description,
            resource = resource,
            action = action,
            isActive = isActive,
            createdAt = LocalDateTime.now()
        )
    }
} 