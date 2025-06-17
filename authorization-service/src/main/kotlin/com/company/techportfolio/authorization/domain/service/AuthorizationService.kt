package com.company.techportfolio.authorization.domain.service

import com.company.techportfolio.authorization.domain.model.AuthorizationRequest
import com.company.techportfolio.authorization.domain.model.AuthorizationResponse
import com.company.techportfolio.authorization.domain.model.UserPermissions
import com.company.techportfolio.authorization.domain.port.UserRepository
import com.company.techportfolio.authorization.domain.port.RoleRepository
import com.company.techportfolio.authorization.domain.port.PermissionRepository
import com.company.techportfolio.shared.domain.model.User
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository
) {

    fun authorizeUser(request: AuthorizationRequest): AuthorizationResponse {
        return try {
            val username = request.username
            val resource = request.resource
            val action = request.action

            // Check if user exists and is active
            if (!userRepository.isUserActive(username)) {
                return AuthorizationResponse.unauthorized(
                    username = username,
                    resource = resource,
                    action = action,
                    errorMessage = "User is not active or does not exist"
                )
            }

            // Check if user has the required permission
            val hasPermission = permissionRepository.hasPermission(username, resource, action)
            
            if (hasPermission) {
                val permissions = userRepository.findUserPermissions(username)
                val roles = userRepository.findUserRoles(username)
                val organizationId = userRepository.findUserOrganization(username)

                AuthorizationResponse.authorized(
                    username = username,
                    resource = resource,
                    action = action,
                    permissions = permissions,
                    roles = roles,
                    organizationId = organizationId
                )
            } else {
                AuthorizationResponse.unauthorized(
                    username = username,
                    resource = resource,
                    action = action,
                    errorMessage = "User does not have permission for $resource:$action"
                )
            }
        } catch (e: Exception) {
            AuthorizationResponse.unauthorized(
                username = request.username,
                resource = request.resource,
                action = request.action,
                errorMessage = "Authorization failed: ${e.message}"
            )
        }
    }

    fun getUserPermissions(username: String): UserPermissions {
        return try {
            if (!userRepository.isUserActive(username)) {
                return UserPermissions(
                    username = username,
                    permissions = emptyList(),
                    roles = emptyList(),
                    isActive = false
                )
            }

            val permissions = userRepository.findUserPermissions(username)
            val roles = userRepository.findUserRoles(username)
            val organizationId = userRepository.findUserOrganization(username)

            UserPermissions(
                username = username,
                permissions = permissions,
                roles = roles,
                organizationId = organizationId,
                isActive = true
            )
        } catch (e: Exception) {
            UserPermissions(
                username = username,
                permissions = emptyList(),
                roles = emptyList(),
                isActive = false
            )
        }
    }

    fun hasRole(username: String, role: String): Boolean {
        return try {
            if (!userRepository.isUserActive(username)) {
                return false
            }

            val userRoles = userRepository.findUserRoles(username)
            userRoles.contains(role)
        } catch (e: Exception) {
            false
        }
    }

    fun hasAnyRole(username: String, roles: List<String>): Boolean {
        return try {
            if (!userRepository.isUserActive(username)) {
                return false
            }

            val userRoles = userRepository.findUserRoles(username)
            userRoles.any { it in roles }
        } catch (e: Exception) {
            false
        }
    }

    fun hasPermission(username: String, resource: String, action: String): Boolean {
        return try {
            if (!userRepository.isUserActive(username)) {
                return false
            }

            permissionRepository.hasPermission(username, resource, action)
        } catch (e: Exception) {
            false
        }
    }

    fun hasAnyPermission(username: String, resource: String, actions: List<String>): Boolean {
        return try {
            if (!userRepository.isUserActive(username)) {
                return false
            }

            actions.any { action -> permissionRepository.hasPermission(username, resource, action) }
        } catch (e: Exception) {
            false
        }
    }

    fun getUserDetails(username: String): AuthorizationResponse {
        return try {
            if (!userRepository.isUserActive(username)) {
                return AuthorizationResponse.unauthorized(
                    username = username,
                    resource = null,
                    action = null,
                    errorMessage = "User is not active or does not exist"
                )
            }

            val user = userRepository.findById(username)
            if (user == null) {
                return AuthorizationResponse.unauthorized(
                    username = username,
                    resource = null,
                    action = null,
                    errorMessage = "User not found"
                )
            }

            val roles = userRepository.findUserRoles(username)

            AuthorizationResponse.authorized(
                username = username,
                resource = null,
                action = null,
                permissions = emptyList(),
                roles = roles,
                organizationId = user.organizationId
            )
        } catch (e: Exception) {
            AuthorizationResponse.unauthorized(
                username = username,
                resource = null,
                action = null,
                errorMessage = "Authorization failed: ${e.message}"
            )
        }
    }
} 