package hs.kr.entrydsm.domain.user.value

/**
 * 사용자 역할을 나타내는 열거형 클래스입니다.
 * gRPC 프로토콜과 매핑되는 사용자 권한을 정의합니다.
 */
enum class UserRole {
    /**
     * 최고 관리자 권한
     */
    ROOT,

    /**
     * 일반 사용자 권한
     */
    USER,

    /**
     * 관리자 권한
     */
    ADMIN,
}