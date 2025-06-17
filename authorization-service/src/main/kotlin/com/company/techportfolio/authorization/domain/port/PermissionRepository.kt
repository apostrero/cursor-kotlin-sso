package com.company.techportfolio.authorization.domain.port

import com.company.techportfolio.shared.domain.model.Permission

interface PermissionRepository {
    fun findById(id: Long): Permission?
    fun findByName(name: String): Permission?
    fun findAll(): List<Permission>
    fun save(permission: Permission): Permission
    fun delete(id: Long): Boolean
    fun findByResourceAndAction(resource: String, action: String): Permission?
    fun findPermissionsByUser(username: String): List<Permission>
    fun hasPermission(username: String, resource: String, action: String): Boolean
} 