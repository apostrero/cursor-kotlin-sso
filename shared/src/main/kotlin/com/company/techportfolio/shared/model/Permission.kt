package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * JPA entity representing a permission in the authorization system.
 * 
 * This entity maps to the "permissions" database table and represents specific
 * permissions that define what actions can be performed on what resources.
 * Permissions are the atomic units of authorization control.
 * 
 * @property id Unique identifier for the permission (auto-generated)
 * @property name Unique name of the permission (required)
 * @property description Optional description of the permission's purpose
 * @property resource The resource type this permission applies to (required)
 * @property action The action this permission allows (required)
 * @property isActive Whether the permission is currently active
 * @property createdAt Timestamp when the permission was created
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Entity
@Table(name = "permissions")
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Permission name is required")
    @field:Size(max = 100, message = "Permission name must not exceed 100 characters")
    @Column(unique = true, nullable = false)
    val name: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Column
    val description: String? = null,

    @field:NotBlank(message = "Resource is required")
    @field:Size(max = 100, message = "Resource must not exceed 100 characters")
    @Column(nullable = false)
    val resource: String,

    @field:NotBlank(message = "Action is required")
    @field:Size(max = 50, message = "Action must not exceed 50 characters")
    @Column(nullable = false)
    val action: String,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Generates a permission string in the format "resource:action".
     * 
     * This method provides a standardized way to represent permissions
     * as strings, which is useful for authorization checks and logging.
     * 
     * @return Permission string in the format "resource:action"
     */
    fun getPermissionString(): String = "$resource:$action"
} 