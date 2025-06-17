package com.company.techportfolio.authorization.domain.port

import com.company.techportfolio.shared.domain.model.Role
import com.company.techportfolio.shared.domain.model.Permission

interface RoleRepository {
    fun findById(id: Long): Role?
    fun findByName(name: String): Role?
    fun findAll(): List<Role>
    fun save(role: Role): Role
    fun delete(id: Long): Boolean
    fun findRolesByUser(username: String): List<Role>
    fun findPermissionsByRole(roleName: String): List<Permission>
} 