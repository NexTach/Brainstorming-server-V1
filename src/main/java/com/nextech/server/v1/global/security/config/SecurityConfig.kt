package com.nextech.server.v1.global.security.config

import com.nextech.server.v1.global.security.filter.JwtFilter
import com.nextech.server.v1.global.security.jwt.service.JwtAuthenticationService
import com.nextech.server.v1.global.security.jwt.service.JwtTokenService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
open class SecurityConfig(
    private val jwtTokenService: JwtTokenService,
    private val jwtAuthenticationService: JwtAuthenticationService
) {

    @Bean
    open fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }.cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests.requestMatchers(
                    "/auth/signin",
                    "auth/signup",
                    "auth/reissue").permitAll().anyRequest()
                    .authenticated()
            }.addFilterBefore(JwtFilter(jwtTokenService,jwtAuthenticationService), UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfig = CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "refreshToken")
            allowCredentials = false
            exposedHeaders = listOf("Authorization")
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfig)
        }
        return source
    }
}