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

@Configuration
@EnableWebSecurity
@Profile("mock-auth")
class MockAuthConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

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

    @Bean
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