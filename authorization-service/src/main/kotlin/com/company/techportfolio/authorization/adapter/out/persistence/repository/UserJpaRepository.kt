package com.company.techportfolio.authorization.adapter.out.persistence.repository

import com.company.techportfolio.authorization.adapter.out.persistence.entity.UserEntity
import com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity
import com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity?
    fun findByUsernameAndIsActiveTrue(username: String): UserEntity?
    
    @Query("SELECT u.organizationId FROM UserEntity u WHERE u.username = :username")
    fun findOrganizationIdByUsername(@Param("username") username: String): Long?
    
    @Query("SELECT u.isActive FROM UserEntity u WHERE u.username = :username")
    fun isUserActive(@Param("username") username: String): Boolean?
}

@Repository
interface RoleJpaRepository : JpaRepository<RoleEntity, Long> {
    fun findByName(name: String): RoleEntity?
    fun findByNameAndIsActiveTrue(name: String): RoleEntity?
    
    @Query("SELECT r FROM RoleEntity r JOIN r.users u WHERE u.username = :username AND r.isActive = true")
    fun findRolesByUsername(@Param("username") username: String): List<RoleEntity>
}

@Repository
interface PermissionJpaRepository : JpaRepository<PermissionEntity, Long> {
    fun findByResourceAndAction(resource: String, action: String): PermissionEntity?
    fun findByResourceAndActionAndIsActiveTrue(resource: String, action: String): PermissionEntity?
    
    @Query("SELECT DISTINCT p FROM PermissionEntity p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.username = :username AND p.isActive = true AND r.isActive = true")
    fun findPermissionsByUsername(@Param("username") username: String): List<PermissionEntity>
    
    @Query("SELECT COUNT(p) > 0 FROM PermissionEntity p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.username = :username AND p.resource = :resource AND p.action = :action " +
           "AND p.isActive = true AND r.isActive = true")
    fun hasPermission(
        @Param("username") username: String,
        @Param("resource") resource: String,
        @Param("action") action: String
    ): Boolean
} 