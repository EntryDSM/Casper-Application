package hs.kr.entrydsm.application.global.security

import hs.kr.entrydsm.application.global.exception.GlobalExceptions
import hs.kr.entrydsm.application.global.security.spi.SecurityPort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.util.UUID

@Component
class SecurityAdapter : SecurityPort {
    override fun getCurrentUserId(): UUID {
        val userId = SecurityContextHolder.getContext().authentication.name
        try {
            return UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            throw GlobalExceptions.InvalidTokenException()
        }
    }
}
