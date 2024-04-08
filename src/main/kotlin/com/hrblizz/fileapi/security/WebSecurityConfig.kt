package com.hrblizz.fileapi.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher


@Configuration
class WebSecurityConfig(
    private val apiAuthenticationEntryPoint: ApiAuthenticationEntryPoint,
    private val apiAuthenticationProvider: AuthenticationProvider
) {
    companion object {
        private val ALLOWED_PATHS = listOf(
            "/docs", "/docs/**", "/webjars/**", "/favicon.ico", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**"
        )
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .httpBasic { it.authenticationEntryPoint(apiAuthenticationEntryPoint) }
            .csrf { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(*ALLOWED_PATHS.map { AntPathRequestMatcher(it) }.toTypedArray())
                    .permitAll()
                    .anyRequest()
                    .fullyAuthenticated()
            }
            .authenticationProvider(apiAuthenticationProvider)

        return http.build()
    }
}
