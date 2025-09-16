package hs.kr.entrydsm.domain.status.interfaces

import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.status.aggregates.StatusCache

/**
 * 원서 상태 조회를 위한 계약 인터페이스입니다.
 * 
 * 접수번호를 기반으로 원서 상태 정보를 조회하는 기능을 정의하며,
 * 일반 조회와 캐시 기반 조회를 모두 지원합니다.
 */
interface ApplicationQueryStatusContract {
    
    /**
     * 접수번호로 원서 상태를 조회합니다.
     * 
     * 외부 Status 서비스에서 해당 접수번호의 상태 정보를 조회합니다.
     * 
     * @param receiptCode 조회할 접수번호
     * @return 조회된 상태 정보, 존재하지 않는 경우 null
     */
    fun queryStatusByReceiptCode(receiptCode: Long): Status?
    
    /**
     * 캐시에서 접수번호로 원서 상태를 조회합니다.
     * 
     * Redis 등의 캐시 스토리지에서 해당 접수번호의 상태 정보를 조회하여
     * 빠른 응답을 제공합니다.
     * 
     * @param receiptCode 조회할 접수번호
     * @return 캐시된 상태 정보, 존재하지 않는 경우 null
     */
    fun queryStatusByReceiptCodeInCache(receiptCode: Long): StatusCache?
}
