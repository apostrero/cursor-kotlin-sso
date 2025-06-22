package com.company.techportfolio.shared.domain.port

import com.company.techportfolio.shared.domain.model.Organization
import com.company.techportfolio.shared.domain.model.Role
import com.company.techportfolio.shared.domain.model.User

interface UserRepository {
    fun findById(id: Long): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun save(user: User): User
    fun update(user: User): User
    fun delete(id: Long): Boolean
    fun findAll(): List<User>
    fun findByOrganizationId(organizationId: Long): List<User>
    fun findByRole(roleName: String): List<User>
    fun updateLastLoginAt(username: String): Boolean
}

interface OrganizationRepository {
    fun findById(id: Long): Organization?
    fun findByName(name: String): Organization?
    fun save(organization: Organization): Organization
    fun update(organization: Organization): Organization
    fun delete(id: Long): Boolean
    fun findAll(): List<Organization>
    fun findByParentId(parentId: Long): List<Organization>
}

interface RoleRepository {
    fun findById(id: Long): Role?
    fun findByName(name: String): Role?
    fun save(role: Role): Role
    fun update(role: Role): Role
    fun delete(id: Long): Boolean
    fun findAll(): List<Role>
    fun findByPermission(permission: String): List<Role>
} 