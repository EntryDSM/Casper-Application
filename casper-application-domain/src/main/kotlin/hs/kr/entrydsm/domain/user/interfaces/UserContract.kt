package hs.kr.entrydsm.domain.user.interfaces

/**
 * 사용자 관련 외부 통신을 위한 계약 인터페이스입니다.
 * 
 * 현재는 ApplicationQueryUserContract를 상속받아 
 * 원서 조회 시 필요한 사용자 정보 조회 기능을 제공합니다.
 */
interface UserContract : ApplicationQueryUserContract {
}
