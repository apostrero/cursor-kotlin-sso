package com.company.techportfolio.authorization.domain.port

import com.company.techportfolio.shared.domain.model.User

interface UserRepository {
    fun findById(username: String): User?
    fun findByUsername(username: String): User?
    fun findUserWithRolesAndPermissions(username: String): User?
    fun findUserPermissions(username: String): List<String>
    fun findUserRoles(username: String): List<String>
    fun isUserActive(username: String): Boolean
    fun findUserOrganization(username: String): Long?
} 
 