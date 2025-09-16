package hs.kr.entrydsm.application.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Spring Security 설정 클래스입니다.
 * 애플리케이션의 보안 정책과 인증/인가 규칙을 정의합니다.
 */
@Configuration
class SecurityConfig {
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
            .cors { it.disable() }
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
                    .anyRequest().authenticated()
            }

        return http.build()
    }
}
