package com.company.techportfolio.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class SamlConfig {

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

    @Bean
    fun logoutSuccessHandler(): LogoutSuccessHandler {
        return LogoutSuccessHandler { request, response, authentication ->
            response.sendRedirect("/saml/login")
        }
    }
} 