package com.company.techportfolio.authorization.adapter.out.persistence

import com.company.techportfolio.authorization.domain.port.RoleRepository
import com.company.techportfolio.shared.domain.model.Role
import com.company.techportfolio.shared.domain.model.Permission
import com.company.techportfolio.authorization.adapter.out.persistence.repository.RoleJpaRepository
import org.springframework.stereotype.Component

@Component
class RoleRepositoryAdapter(
    private val roleJpaRepository: RoleJpaRepository
) : RoleRepository {

    override fun findById(id: Long): Role? {
        return roleJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByName(name: String): Role? {
        return roleJpaRepository.findByName(name)?.toDomain()
    }

    override fun findAll(): List<Role> {
        return roleJpaRepository.findAll().map { it.toDomain() }
    }

    override fun save(role: Role): Role {
        val entity = role.toEntity()
        val savedEntity = roleJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun delete(id: Long): Boolean {
        return if (roleJpaRepository.existsById(id)) {
            roleJpaRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun findRolesByUser(username: String): List<Role> {
        return roleJpaRepository.findRolesByUsername(username)
            .map { it.toDomain() }
    }

    override fun findPermissionsByRole(roleName: String): List<Permission> {
        val role = roleJpaRepository.findByName(roleName)
        return role?.permissions?.map { it.toDomain() } ?: emptyList()
    }

    // Extension functions for mapping between entities and domain models
    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity.toDomain(): Role {
        return Role(
            id = this.id,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            createdAt = this.createdAt,
            permissions = this.permissions.map { it.toDomain() }.toSet()
        )
    }

    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity.toDomain(): Permission {
        return Permission(
            id = this.id,
            name = this.name,
            description = this.description,
            resource = this.resource,
            action = this.action,
            isActive = this.isActive,
            createdAt = this.createdAt
        )
    }

    // Extension function for mapping domain model to entity
    private fun Role.toEntity(): com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity {
        return com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            createdAt = this.createdAt,
            permissions = emptySet() // This would need to be handled properly in a real implementation
        )
    }
} 