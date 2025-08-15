package hs.kr.entrydsm.global.interfaces

/**
 * 도메인 서비스를 위한 인터페이스입니다.
 *
 * 도메인 로직을 캡슐화하되 상태는 가지지 않는 서비스를 정의합니다.
 */
interface DomainService {
    
    /**
     * 서비스 이름을 반환합니다.
     *
     * @return 서비스 이름
     */
    fun getServiceName(): String
    
    /**
     * 서비스가 지원하는 작업들을 반환합니다.
     *
     * @return 지원하는 작업 목록
     */
    fun getSupportedOperations(): Set<String>
}