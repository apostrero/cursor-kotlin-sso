package com.company.techportfolio.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

/**
 * Mock authentication configuration for development and testing environments.
 *
 * This configuration class provides a complete mock authentication system that
 * simulates SAML SSO functionality without requiring external identity providers.
 * It is only active when running with the 'mock-auth' profile.
 *
 * Key features:
 * - In-memory user store with predefined test users
 * - BCrypt password encoding for security
 * - Role-based access control (RBAC) with permissions
 * - Form-based authentication with custom login page
 * - Session management and logout functionality
 * - Security filter chain configuration
 *
 * Predefined test users:
 * - user1/password (Portfolio Manager role)
 * - user2/password (Viewer role)
 * - admin/secret (Administrator role)
 *
 * Profile activation:
 * - Only active when 'mock-auth' profile is enabled
 * - Provides alternative to SAML SSO for development
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@Profile("mock-auth")
class MockAuthConfig {

    /**
     * Configures password encoder for secure password hashing.
     *
     * Uses BCrypt algorithm for password encoding, which is a secure
     * one-way hashing function with salt generation. This ensures that
     * even in mock mode, passwords are properly secured.
     *
     * @return PasswordEncoder instance using BCrypt algorithm
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Configures in-memory user details service with predefined test users.
     *
     * Creates an in-memory user store with three predefined users that have
     * different roles and permissions. This simulates the user data that
     * would typically come from SAML identity providers.
     *
     * User configuration:
     * - user1: Portfolio Manager with read/write/analytics permissions
     * - user2: Viewer with read-only permissions
     * - admin: Administrator with full permissions
     *
     * All users have BCrypt-encoded passwords for security.
     *
     * @return UserDetailsService with in-memory user store
     */
    @Bean
    fun userDetailsService(): UserDetailsService {
        val users = mutableListOf<UserDetails>()

        // Mock users matching the SimpleSAMLphp test users
        users.add(
            User.builder()
                .username("user1")
                .password(passwordEncoder().encode("password"))
                .authorities(
                    SimpleGrantedAuthority("ROLE_PORTFOLIO_MANAGER"),
                    SimpleGrantedAuthority("READ_PORTFOLIO"),
                    SimpleGrantedAuthority("WRITE_PORTFOLIO"),
                    SimpleGrantedAuthority("VIEW_ANALYTICS")
                )
                .build()
        )

        users.add(
            User.builder()
                .username("user2")
                .password(passwordEncoder().encode("password"))
                .authorities(
                    SimpleGrantedAuthority("ROLE_VIEWER"),
                    SimpleGrantedAuthority("READ_PORTFOLIO")
                )
                .build()
        )

        users.add(
            User.builder()
                .username("admin")
                .password(passwordEncoder().encode("secret"))
                .authorities(
                    SimpleGrantedAuthority("ROLE_ADMIN"),
                    SimpleGrantedAuthority("READ_PORTFOLIO"),
                    SimpleGrantedAuthority("WRITE_PORTFOLIO"),
                    SimpleGrantedAuthority("DELETE_PORTFOLIO"),
                    SimpleGrantedAuthority("MANAGE_USERS"),
                    SimpleGrantedAuthority("VIEW_ANALYTICS")
                )
                .build()
        )

        return InMemoryUserDetailsManager(users)
    }

    /**
     * Configures Spring Security filter chain for mock authentication.
     *
     * Sets up the complete security configuration including:
     * - URL-based authorization rules
     * - Form-based login with custom pages
     * - Logout handling
     * - CSRF protection (disabled for API endpoints)
     * - Frame options for embedding
     *
     * Authorization rules:
     * - Public endpoints: actuator, health, API auth endpoints, login pages
     * - All other endpoints require authentication
     *
     * Login configuration:
     * - Custom login page at /mock-login
     * - Success redirect to /api/auth/mock-success
     * - Failure redirect with error parameter
     *
     * @param http HttpSecurity configuration builder
     * @return SecurityFilterChain with complete security configuration
     */
    @Bean
    @Suppress("DEPRECATION")
    fun mockAuthSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/**", "/health", "/info").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/mock-login", "/mock-logout").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .loginPage("/mock-login")
                    .loginProcessingUrl("/mock-login")
                    .defaultSuccessUrl("/api/auth/mock-success", true)
                    .failureUrl("/mock-login?error=true")
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/mock-logout")
                    .logoutSuccessUrl("/mock-login?logout=true")
                    .permitAll()
            }
            .csrf { csrf -> csrf.disable() }
            .headers { headers -> headers.frameOptions().sameOrigin() }
            .build()
    }
} 