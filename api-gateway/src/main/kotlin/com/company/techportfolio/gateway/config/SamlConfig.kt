package com.company.techportfolio.gateway.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

/**
 * SAML 2.0 authentication configuration for production environments.
 *
 * This configuration class provides SAML 2.0 Single Sign-On (SSO) authentication
 * functionality for integration with enterprise identity providers. It is only
 * active when NOT running in 'mock-auth' profile, providing production-ready
 * authentication capabilities.
 *
 * Key features:
 * - SAML 2.0 SSO authentication with external identity providers
 * - Configurable login and logout endpoints
 * - Security filter chain with authorization rules
 * - Custom logout success handling
 * - CSRF protection disabled for API endpoints
 * - Public endpoints for health checks and actuator
 *
 * SAML endpoints:
 * - Login: /saml/login
 * - Logout: /saml/logout
 * - Success redirect: /dashboard
 * - Failure redirect: /saml/login?error=true
 *
 * Profile activation:
 * - Active when profile is NOT 'mock-auth'
 * - Provides production SAML SSO authentication
 * - Complements MockAuthConfig for different environments
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@Profile("!mock-auth")  // Only load when NOT in mock-auth mode
@ConditionalOnProperty(name = ["spring.security.saml2.enabled"], havingValue = "true", matchIfMissing = false)
class SamlConfig {

    /**
     * Configures Spring Security filter chain for SAML 2.0 authentication.
     *
     * Sets up the complete security configuration including:
     * - URL-based authorization rules
     * - SAML 2.0 login configuration with custom endpoints
     * - Logout handling with custom success handler
     * - CSRF protection disabled for API compatibility
     *
     * Authorization rules:
     * - Public endpoints: actuator, health, metrics, auth health check
     * - All other endpoints require SAML authentication
     *
     * SAML configuration:
     * - Custom login page at /saml/login
     * - Success redirect to /dashboard
     * - Failure redirect with error parameter
     * - Logout endpoint at /saml/logout
     *
     * @param http HttpSecurity configuration builder
     * @return SecurityFilterChain with complete SAML security configuration
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/actuator/**", "/health", "/metrics", "/api/auth/health").permitAll()
                    .anyRequest().authenticated()
            }
            .saml2Login { saml2 ->
                saml2
                    .loginPage("/saml/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/saml/login?error=true")
            }
            .logout { logout ->
                logout
                    .logoutRequestMatcher(AntPathRequestMatcher("/saml/logout"))
                    .logoutSuccessHandler(logoutSuccessHandler())
            }
            .csrf { csrf -> csrf.disable() }

        return http.build()
    }

    /**
     * Configures custom logout success handler for SAML logout operations.
     *
     * Provides a custom logout success handler that redirects users to the
     * SAML login page after successful logout. This ensures proper logout
     * flow and user experience in SAML SSO environments.
     *
     * The handler performs:
     * - Redirect to /saml/login after successful logout
     * - Proper session cleanup and security context clearing
     * - Integration with SAML identity provider logout flows
     *
     * @return LogoutSuccessHandler configured for SAML logout redirect
     */
    @Bean
    fun logoutSuccessHandler(): LogoutSuccessHandler {
        return LogoutSuccessHandler { _, response, _ ->
            response.sendRedirect("/saml/login")
        }
    }
} 