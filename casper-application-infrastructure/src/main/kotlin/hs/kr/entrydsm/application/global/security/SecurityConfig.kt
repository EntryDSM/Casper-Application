package hs.kr.entrydsm.application.global.security

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.global.error.GlobalExceptionFilter
import hs.kr.entrydsm.application.global.security.jwt.JwtFilter
import hs.kr.entrydsm.application.global.security.jwt.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsUtils

@Configuration
class SecurityConfig(
    private val objectMapper: ObjectMapper,
) {
    @Bean
    protected fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        http.authorizeHttpRequests {
            it.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
            it.requestMatchers("/").permitAll()
            it.requestMatchers("/admin/**").hasRole(UserRole.ADMIN.name)
            it.requestMatchers("/schools").permitAll()
            it.requestMatchers("/score/**").hasRole(UserRole.USER.name)
            it.anyRequest().authenticated()
        }

        http
            .addFilterBefore(JwtFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(GlobalExceptionFilter(objectMapper), JwtFilter::class.java)

        return http.build()
    }
}
