package com.company.techportfolio.authorization.adapter.out.persistence

import com.company.techportfolio.authorization.domain.port.UserRepository
import com.company.techportfolio.shared.domain.model.User
import com.company.techportfolio.authorization.adapter.out.persistence.repository.UserJpaRepository
import com.company.techportfolio.authorization.adapter.out.persistence.repository.RoleJpaRepository
import com.company.techportfolio.authorization.adapter.out.persistence.repository.PermissionJpaRepository
import org.springframework.stereotype.Component

@Component
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository,
    private val roleJpaRepository: RoleJpaRepository,
    private val permissionJpaRepository: PermissionJpaRepository
) : UserRepository {

    override fun findById(username: String): User? {
        return userJpaRepository.findByUsername(username)?.toDomain()
    }

    override fun findByUsername(username: String): User? {
        return userJpaRepository.findByUsername(username)?.toDomain()
    }

    override fun findUserWithRolesAndPermissions(username: String): User? {
        return userJpaRepository.findByUsername(username)?.toDomain()
    }

    override fun findUserPermissions(username: String): List<String> {
        return permissionJpaRepository.findPermissionsByUsername(username)
            .map { it.getPermissionString() }
    }

    override fun findUserRoles(username: String): List<String> {
        return roleJpaRepository.findRolesByUsername(username)
            .map { it.name }
    }

    override fun isUserActive(username: String): Boolean {
        return userJpaRepository.isUserActive(username) ?: false
    }

    override fun findUserOrganization(username: String): Long? {
        return userJpaRepository.findOrganizationIdByUsername(username)
    }

    // Extension function for mapping between entity and domain model
    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.UserEntity.toDomain(): User {
        return User(
            id = this.id,
            username = this.username,
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            isActive = this.isActive,
            isEnabled = this.isEnabled,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lastLoginAt = this.lastLoginAt,
            organizationId = this.organizationId,
            roles = this.roles.map { it.toDomain() }.toSet()
        )
    }

    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity.toDomain(): com.company.techportfolio.shared.domain.model.Role {
        return com.company.techportfolio.shared.domain.model.Role(
            id = this.id,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            createdAt = this.createdAt,
            permissions = this.permissions.map { it.toDomain() }.toSet()
        )
    }

    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity.toDomain(): com.company.techportfolio.shared.domain.model.Permission {
        return com.company.techportfolio.shared.domain.model.Permission(
            id = this.id,
            name = this.name,
            description = this.description,
            resource = this.resource,
            action = this.action,
            isActive = this.isActive,
            createdAt = this.createdAt
        )
    }
} 