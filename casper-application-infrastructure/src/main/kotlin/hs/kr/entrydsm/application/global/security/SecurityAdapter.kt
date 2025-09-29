package hs.kr.entrydsm.application.global.security

import hs.kr.entrydsm.domain.security.exceptions.SecurityException
import hs.kr.entrydsm.domain.security.interfaces.SecurityContract
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Spring Security와 도메인 계층을 연결하는 어댑터입니다.
 *
 * SecurityContract의 구현체로, Spring Security Context에서
 * 현재 인증된 사용자 정보를 추출하여 도메인 계층에 제공합니다.
 */
@Component
class SecurityAdapter : SecurityContract {
    override fun getCurrentUserId(): UUID {
        val authentication =
            SecurityContextHolder.getContext().authentication
                ?: throw SecurityException.UnauthenticatedException("인증 컨텍스트가 존재하지 않습니다")

        val userId =
            authentication.name
                ?: throw SecurityException.UnauthenticatedException("사용자 정보가 존재하지 않습니다")

        try {
            return UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            throw SecurityException.InvalidTokenException(userId)
        }
    }
}
