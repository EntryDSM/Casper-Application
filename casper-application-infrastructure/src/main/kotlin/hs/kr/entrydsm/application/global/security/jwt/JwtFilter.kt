package hs.kr.entrydsm.application.global.security.jwt

import hs.kr.entrydsm.domain.user.value.UserRole
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 인증을 처리하는 필터입니다.
 * 
 * Gateway에서 JWT를 파싱하여 헤더로 전달받은 사용자 정보를 
 * Spring Security Context에 설정합니다.
 */
class JwtFilter : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val userId: String? = request.getHeader("Request-User-Id")
        val role: UserRole? = request.getHeader("Request-User-Role")?.let { 
            try {
                UserRole.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        if (userId == null || role == null) {
            filterChain.doFilter(request, response)
            return
        }

        val authorities = mutableListOf(SimpleGrantedAuthority("ROLE_${role.name}"))
        val userDetails: UserDetails = User(userId, "", authorities)
        val authentication: Authentication =
            UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)

        SecurityContextHolder.clearContext()
        SecurityContextHolder.getContext().authentication = authentication
        
        filterChain.doFilter(request, response)
    }
}
