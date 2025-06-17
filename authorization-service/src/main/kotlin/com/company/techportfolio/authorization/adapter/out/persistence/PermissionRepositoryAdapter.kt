package com.company.techportfolio.authorization.adapter.out.persistence

import com.company.techportfolio.authorization.domain.port.PermissionRepository
import com.company.techportfolio.shared.domain.model.Permission
import com.company.techportfolio.authorization.adapter.out.persistence.repository.PermissionJpaRepository
import org.springframework.stereotype.Component

@Component
class PermissionRepositoryAdapter(
    private val permissionJpaRepository: PermissionJpaRepository
) : PermissionRepository {

    override fun findById(id: Long): Permission? {
        return permissionJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByName(name: String): Permission? {
        // This method doesn't exist in the JPA repository, so we'll need to implement it
        // For now, return null as it's not used in the current implementation
        return null
    }

    override fun findAll(): List<Permission> {
        return permissionJpaRepository.findAll().map { it.toDomain() }
    }

    override fun save(permission: Permission): Permission {
        val entity = permission.toEntity()
        val savedEntity = permissionJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun delete(id: Long): Boolean {
        return if (permissionJpaRepository.existsById(id)) {
            permissionJpaRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun findByResourceAndAction(resource: String, action: String): Permission? {
        return permissionJpaRepository.findByResourceAndAction(resource, action)?.toDomain()
    }

    override fun findPermissionsByUser(username: String): List<Permission> {
        return permissionJpaRepository.findPermissionsByUsername(username)
            .map { it.toDomain() }
    }

    override fun hasPermission(username: String, resource: String, action: String): Boolean {
        return permissionJpaRepository.hasPermission(username, resource, action)
    }

    // Extension functions for mapping between entities and domain models
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
    private fun Permission.toEntity(): com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity {
        return com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity(
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