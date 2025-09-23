package hs.kr.entrydsm.domain.user.interfaces

import hs.kr.entrydsm.domain.user.aggregates.User
import java.util.UUID

/**
 * 원서 조회 시 필요한 사용자 정보 조회를 위한 계약 인터페이스입니다.
 * 
 * 원서와 연관된 사용자 정보를 외부 서비스로부터 조회하는 기능을 정의합니다.
 */
interface ApplicationQueryUserContract {
    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     * 
     * @param userId 조회할 사용자의 고유 식별자
     * @return 조회된 사용자 정보
     */
    fun queryUserByUserId(userId: UUID): User
    
    /**
     * 여러 사용자 ID로 사용자 정보 목록을 조회합니다.
     * 
     * @param userIds 조회할 사용자들의 고유 식별자 목록
     * @return 조회된 사용자 정보 목록
     */
    fun queryUsersByIds(userIds: List<UUID>): List<User>
}
