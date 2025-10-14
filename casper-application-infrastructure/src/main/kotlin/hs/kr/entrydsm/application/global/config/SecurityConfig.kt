package hs.kr.entrydsm.application.global.config

import hs.kr.entrydsm.application.global.security.FilterConfig
import hs.kr.entrydsm.application.global.security.jwt.JwtProperties
import hs.kr.entrydsm.domain.user.value.UserRole
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

/**
 * Spring Security 설정 클래스입니다.
 * 애플리케이션의 보안 정책과 인증/인가 규칙을 정의합니다.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val filterConfig: FilterConfig,
) {
    /**
     * Spring Security 필터 체인을 구성합니다.
     * HTTP 보안 설정 및 경로별 접근 권한을 정의합니다.
     *
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     */
    @Bean
    protected fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { cors ->
                cors.configurationSource { request ->
                    val configuration = CorsConfiguration()
                    configuration.allowedOriginPatterns = listOf("*")
                    configuration.allowedMethods = listOf("*")
                    configuration.allowedHeaders = listOf("*")
                    configuration.allowCredentials = true
                    configuration
                }
            }
            .formLogin { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-resources/**").permitAll()
                    .requestMatchers("/webjars/**").permitAll()
                    .requestMatchers("/api/v1/public/calculator/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/admin/excel/**").hasRole(UserRole.ADMIN.name)
                    .requestMatchers(HttpMethod.GET, "/api/v1/application/pdf/**").hasRole(UserRole.USER.name)
                    .requestMatchers(HttpMethod.POST, "/api/v1/application/pdf/**").hasRole(UserRole.USER.name)
                    .requestMatchers(HttpMethod.GET, "/api/v1/**").hasRole(UserRole.ADMIN.name)
                    .requestMatchers("/api/v1/applications/**").hasRole(UserRole.USER.name)
                    .requestMatchers("/photo").hasRole(UserRole.USER.name)
                    .requestMatchers("/pass/**").hasRole(UserRole.USER.name)
                    .requestMatchers("/application/**").hasRole(UserRole.USER.name)
                    .requestMatchers("/schools/**").permitAll()
                    .requestMatchers("/exam-code").hasRole(UserRole.ADMIN.name)
                    .anyRequest().authenticated()
            }
            .with(filterConfig) { }

        return http.build()
    }
}
