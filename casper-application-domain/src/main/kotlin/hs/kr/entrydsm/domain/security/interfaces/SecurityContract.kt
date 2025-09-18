package hs.kr.entrydsm.domain.security.interfaces

import java.util.UUID

/**
 * 보안 관련 기능을 제공하는 계약(Contract)입니다.
 * 
 * 현재 인증된 사용자의 정보를 조회하는 기능을 제공합니다.
 */
interface SecurityContract {
    
    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     * 
     * @return 현재 사용자 ID
     * @throws SecurityException 인증 정보가 없거나 유효하지 않은 경우
     */
    fun getCurrentUserId(): UUID
}
